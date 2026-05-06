package ru.underfish.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.sql.DriverManager
import java.util.Base64
import java.util.UUID

@TestInstance(Lifecycle.PER_CLASS)
class RegistrationE2ETest {
    private val mapper = jacksonObjectMapper()
    private val gatewayBase = System.getenv("GATEWAY_URL") ?: "http://localhost:8080"
    private val authBase = System.getenv("AUTH_SERVICE_URL") ?: "http://localhost:8091"

    private fun getJsonWithBearer(url: String, token: String): Pair<Int, String> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        conn.setRequestProperty("Authorization", "Bearer $token")
        val respCode = conn.responseCode
        val stream = if (respCode in 200..299) conn.inputStream else conn.errorStream
        val respBody = stream?.bufferedReader()?.readText().orEmpty()
        conn.disconnect()
        return Pair(respCode, respBody)
    }

    private fun postJson(url: String, body: Any): Pair<Int, String> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.outputStream.use { os -> os.write(mapper.writeValueAsBytes(body)) }
        val respCode = conn.responseCode
        val stream = if (respCode in 200..299) conn.inputStream else conn.errorStream
        val respBody = stream?.bufferedReader()?.readText().orEmpty()
        conn.disconnect()
        return Pair(respCode, respBody)
    }

    private fun postJsonWithBearer(url: String, body: Any, token: String): Pair<Int, String> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.outputStream.use { os -> os.write(mapper.writeValueAsBytes(body)) }
        val respCode = conn.responseCode
        val stream = if (respCode in 200..299) conn.inputStream else conn.errorStream
        val respBody = stream?.bufferedReader()?.readText().orEmpty()
        conn.disconnect()
        return Pair(respCode, respBody)
    }

    private fun getJson(url: String): Pair<Int, String> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        val respCode = conn.responseCode
        val stream = if (respCode in 200..299) conn.inputStream else conn.errorStream
        val respBody = stream?.bufferedReader()?.readText().orEmpty()
        conn.disconnect()
        return Pair(respCode, respBody)
    }

    private fun jwksPublicKey(): RSAPublicKey {
        val (code, body) = getJson("$authBase/.well-known/jwks.json")
        assertTrue(code in 200..299, "Expected JWKS 2xx, got $code, body: $body")

        val first = mapper.readTree(body).get("keys").first()
        val modulus = BigInteger(1, Base64.getUrlDecoder().decode(first.get("n").asText()))
        val exponent = BigInteger(1, Base64.getUrlDecoder().decode(first.get("e").asText()))
        return KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(modulus, exponent)) as RSAPublicKey
    }

    private fun tokenField(fields: Map<String, Any>, vararg names: String): String =
        names.firstNotNullOfOrNull { fields[it]?.toString() }.orEmpty()

    private fun assertJwtForEmail(token: String, email: String, publicKey: RSAPublicKey): SignedJWT {
        assertTrue(token.isNotBlank())
        val signed = SignedJWT.parse(token)
        assertTrue(signed.verify(RSASSAVerifier(publicKey)), "JWT signature invalid")
        assertEquals(email, signed.jwtClaimsSet.subject)
        return signed
    }

    @Test
    fun `registration and login return jwt tokens and user persisted`() {
        // Preconditions: project already running locally (docker compose up).
        val email = "e2e-${UUID.randomUUID()}@example.com"
        val password = "P@ssw0rd123"
        val req = mapOf("email" to email, "password" to password, "name" to "E2E Test")

        val (registerCode, registerResp) = postJson("$gatewayBase/api/v1/users/register", req)
        assertTrue(registerCode in 200..299, "Expected register 2xx, got $registerCode, body: $registerResp")

        val registered: Map<String, Any> = mapper.readValue(registerResp)
        val registerAccessToken = tokenField(registered, "token", "access_token", "accessToken")
        val registerRefreshToken = tokenField(registered, "refresh_token", "refreshToken")
        assertTrue(registerRefreshToken.isNotBlank(), "Registration response must contain refresh token")

        val publicKey = jwksPublicKey()
        val registeredJwt = assertJwtForEmail(registerAccessToken, email, publicKey)
        assertJwtForEmail(registerRefreshToken, email, publicKey)
        val userId = registeredJwt.jwtClaimsSet.getLongClaim("userId")

        val (profileCode, profileResp) = getJsonWithBearer("$gatewayBase/api/v1/users/$userId", registerAccessToken)
        assertTrue(profileCode in 200..299, "Expected profile 2xx, got $profileCode, body: $profileResp")
        val profile: Map<String, Any> = mapper.readValue(profileResp)
        assertEquals(email, profile["email"])
        assertEquals(userId.toString(), tokenField(profile, "user_id", "userId"))

        val (loginCode, loginResp) = postJson("$gatewayBase/api/v1/users/login", mapOf("email" to email, "password" to password))
        assertTrue(loginCode in 200..299, "Expected login 2xx, got $loginCode, body: $loginResp")

        val loggedIn: Map<String, Any> = mapper.readValue(loginResp)
        val loginAccessToken = tokenField(loggedIn, "token", "access_token", "accessToken")
        val loginRefreshToken = tokenField(loggedIn, "refresh_token", "refreshToken")
        assertJwtForEmail(loginAccessToken, email, publicKey)
        assertJwtForEmail(loginRefreshToken, email, publicKey)

        val (badLoginCode, _) = postJson("$gatewayBase/api/v1/users/login", mapOf("email" to email, "password" to "wrong-password"))
        assertEquals(401, badLoginCode)

        val (refreshCode, refreshResp) = postJson("$gatewayBase/api/v1/tokens/refresh", mapOf("refreshToken" to loginRefreshToken))
        assertTrue(refreshCode in 200..299, "Expected refresh 2xx, got $refreshCode, body: $refreshResp")
        val refreshed: Map<String, Any> = mapper.readValue(refreshResp)
        val refreshedAccessToken = tokenField(refreshed, "token", "access_token", "accessToken")
        assertJwtForEmail(refreshedAccessToken, email, publicKey)
        assertNotEquals(loginRefreshToken, refreshedAccessToken, "Refresh must return an access token, not echo refresh token")

        val dbUrl = System.getenv("TEST_DB_URL") ?: "jdbc:postgresql://localhost:5432/auth_db"
        val dbUser = System.getenv("TEST_DB_USER") ?: "auth_user"
        val dbPass = System.getenv("TEST_DB_PASSWORD") ?: "auth_pass"

        DriverManager.getConnection(dbUrl, dbUser, dbPass).use { conn ->
            conn.prepareStatement("SELECT count(*) FROM users WHERE email = ?").use { ps ->
                ps.setString(1, email)
                ps.executeQuery().use { rs ->
                    rs.next()
                    assertEquals(1, rs.getInt(1))
                }
            }
        }
    }

    @Test
    fun `user can get own attendance ids via me after community event flow`() {
        val organizerEmail = "org-${UUID.randomUUID()}@example.com"
        val attendeeEmail = "att-${UUID.randomUUID()}@example.com"
        val password = "P@ssw0rd123"

        val (organizerRegisterCode, organizerRegisterResp) =
            postJson(
                "$gatewayBase/api/v1/users/register",
                mapOf("email" to organizerEmail, "password" to password, "name" to "Organizer"),
            )
        assertTrue(organizerRegisterCode in 200..299, "Expected organizer register 2xx, got $organizerRegisterCode, body: $organizerRegisterResp")
        val organizerRegistered: Map<String, Any> = mapper.readValue(organizerRegisterResp)
        val organizerToken = tokenField(organizerRegistered, "token", "access_token", "accessToken")

        val (communityCode, communityResp) =
            postJsonWithBearer(
                "$gatewayBase/api/v1/communities",
                mapOf(
                    "name" to "E2E Community ${UUID.randomUUID()}",
                    "description" to "community created in e2e",
                    "isPrivate" to false,
                    "coverUrl" to "https://example.com/cover.png",
                ),
                organizerToken,
            )
        assertTrue(communityCode in 200..299, "Expected community create 2xx, got $communityCode, body: $communityResp")
        val community: Map<String, Any> = mapper.readValue(communityResp)
        val communityId = tokenField(community, "communityId", "community_id")

        val (organizersCode, organizersResp) = getJsonWithBearer("$gatewayBase/api/v1/communities/$communityId/organizers", organizerToken)
        assertTrue(organizersCode in 200..299, "Expected organizers 2xx, got $organizersCode, body: $organizersResp")
        val organizers: List<Map<String, Any>> = mapper.readValue(organizersResp)
        val organizerIds = organizers.map { tokenField(it, "userId", "user_id") }
        assertTrue(organizerIds.isNotEmpty(), "Expected non-empty organizers list")

        val (locationCode, locationResp) =
            postJsonWithBearer(
                "$gatewayBase/api/v1/locations",
                mapOf(
                    "address" to "Moscow, Red Square",
                    "latitude" to 55.7558,
                    "longitude" to 37.6173,
                    "city" to "Moscow",
                    "district" to "Center",
                    "placeName" to "E2E Place",
                    "timezone" to "Europe/Moscow",
                ),
                organizerToken,
            )
        assertTrue(locationCode in 200..299, "Expected location create 2xx, got $locationCode, body: $locationResp")
        val location: Map<String, Any> = mapper.readValue(locationResp)
        val locationId = tokenField(location, "locationId", "location_id")

        val (eventCode, eventResp) =
            postJsonWithBearer(
                "$gatewayBase/api/v1/events",
                mapOf(
                    "title" to "E2E Event ${UUID.randomUUID()}",
                    "description" to "event created by organizer",
                    "startDatetime" to "2030-01-01T10:00:00",
                    "endDatetime" to "2030-01-01T12:00:00",
                    "locationId" to locationId,
                    "status" to "PUBLISHED",
                    "price" to 0.0,
                    "currency" to "RUB",
                    "maxParticipants" to 100,
                    "isOnline" to false,
                ),
                organizerToken,
            )
        assertTrue(eventCode in 200..299, "Expected event create 2xx, got $eventCode, body: $eventResp")
        val event: Map<String, Any> = mapper.readValue(eventResp)
        val eventId = tokenField(event, "eventId", "event_id")

        val (attendeeRegisterCode, attendeeRegisterResp) =
            postJson(
                "$gatewayBase/api/v1/users/register",
                mapOf("email" to attendeeEmail, "password" to password, "name" to "Attendee"),
            )
        assertTrue(attendeeRegisterCode in 200..299, "Expected attendee register 2xx, got $attendeeRegisterCode, body: $attendeeRegisterResp")
        assertTrue(attendeeRegisterResp.isNotBlank())

        val (attendeeLoginCode, attendeeLoginResp) =
            postJson(
                "$gatewayBase/api/v1/users/login",
                mapOf("email" to attendeeEmail, "password" to password),
            )
        assertTrue(attendeeLoginCode in 200..299, "Expected attendee login 2xx, got $attendeeLoginCode, body: $attendeeLoginResp")
        val attendeeLoggedIn: Map<String, Any> = mapper.readValue(attendeeLoginResp)
        val attendeeToken = tokenField(attendeeLoggedIn, "token", "access_token", "accessToken")

        val (attendCode, attendResp) =
            postJsonWithBearer(
                "$gatewayBase/api/v1/events/$eventId/attendance",
                mapOf("status" to "confirmed"),
                attendeeToken,
            )
        assertTrue(attendCode in 200..299, "Expected attendance create 2xx, got $attendCode, body: $attendResp")

        val (myAttendanceCode, myAttendanceResp) = getJsonWithBearer("$gatewayBase/api/v1/events/me/attendence", attendeeToken)
        assertTrue(myAttendanceCode in 200..299, "Expected my attendance 2xx, got $myAttendanceCode, body: $myAttendanceResp")
        val myEventIds: List<Long> = mapper.readValue(myAttendanceResp)
        assertTrue(myEventIds.contains(eventId.toLong()), "Expected event id $eventId in my attendence list, body: $myAttendanceResp")
    }
}
