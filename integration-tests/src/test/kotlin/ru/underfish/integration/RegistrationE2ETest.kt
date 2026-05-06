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
}
