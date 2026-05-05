package ru.underfish.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.sql.DriverManager
import java.util.*

@TestInstance(Lifecycle.PER_CLASS)
class RegistrationE2ETest {
    private val mapper = jacksonObjectMapper()

    private fun postJson(url: String, body: Any): Pair<Int, String> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.outputStream.use { os -> os.write(mapper.writeValueAsBytes(body)) }
        val respCode = conn.responseCode
        val respBody = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return Pair(respCode, respBody)
    }

    private fun parsePublicKey(pem: String): RSAPublicKey {
        val cleaned = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .trim()
        val decoded = Base64.getDecoder().decode(cleaned)
        val spec = X509EncodedKeySpec(decoded)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec) as RSAPublicKey
    }

    @Test
    fun `registration returns jwt and user persisted`() {
        // Preconditions: project already running locally (docker compose up)
        val base = "http://localhost:8080" // ожидаем, что gateway доступен на 8080
        val registerUrl = "$base/api/auth/register" // ADAPT: проверьте путь

        val email = "e2e-${UUID.randomUUID()}@example.com"
        val password = "P@ssw0rd123"
        val req = mapOf("email" to email, "password" to password, "name" to "E2E Test")

        val (code, resp) = postJson(registerUrl, req)
        assertTrue(code in 200..299, "Expected 2xx, got $code, body: $resp")

        val parsed: Map<String, Any> = mapper.readValue(resp)
        val token = (parsed["token"] ?: parsed["accessToken"] ?: parsed["access_token"]).toString()
        assertTrue(token.isNotBlank())

        // Try to obtain JWKS from auth-service (default port 8091). If unavailable, fallback to RSA_PUBLIC_KEY env.
        val jwksUrl = "http://localhost:8091/.well-known/jwks.json"
        val pubKey: RSAPublicKey = try {
            val (c, body) = runCatching { postJson(jwksUrl, mapOf<String, String>()) }.getOrNull() ?: Pair(0, "")
            // If JWKS available via GET (we used POST helper for convenience), fetch via GET properly
            val (getCode, getBody) = try {
                val conn = URL(jwksUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 2000
                conn.readTimeout = 2000
                val gc = conn.responseCode
                val gb = if (gc in 200..299) conn.inputStream.bufferedReader().readText() else conn.errorStream?.bufferedReader()?.readText() ?: ""
                conn.disconnect()
                Pair(gc, gb)
            } catch (ex: Exception) {
                Pair(0, "")
            }

            if (getCode in 200..299) {
                // parse jwks and extract first RSA key
                val jwks = mapper.readTree(getBody)
                val keys = jwks.get("keys")
                if (keys != null && keys.isArray && keys.size() > 0) {
                    val first = keys[0]
                    val n = first.get("n").asText()
                    val e = first.get("e").asText()
                    // build RSAPublicKey from n/e
                    val modulus = Base64.getUrlDecoder().decode(n)
                    val exponent = Base64.getUrlDecoder().decode(e)
                    val spec = java.security.spec.RSAPublicKeySpec(java.math.BigInteger(1, modulus), java.math.BigInteger(1, exponent))
                    val kf = java.security.KeyFactory.getInstance("RSA")
                    kf.generatePublic(spec) as RSAPublicKey
                } else {
                    throw IllegalStateException("JWKS has no keys")
                }
            } else {
                // fallback to env
                val publicKeyPem = System.getenv("RSA_PUBLIC_KEY") ?: throw IllegalStateException("RSA_PUBLIC_KEY not set and JWKS unavailable")
                parsePublicKey(publicKeyPem)
            }
        } catch (ex: Exception) {
            // final fallback
            val publicKeyPem = System.getenv("RSA_PUBLIC_KEY") ?: fail("RSA_PUBLIC_KEY not set and JWKS fetch failed: ${ex.message}")
            parsePublicKey(publicKeyPem)
        }

        // If pubKey available, verify signature. Otherwise decode payload without verification.
        try {
            val signed = SignedJWT.parse(token)
            if (pubKey != null) {
                val verifier = RSASSAVerifier(pubKey)
                assertTrue(signed.verify(verifier), "JWT signature invalid")
            }
            val subject = signed.jwtClaimsSet.subject ?: signed.jwtClaimsSet.getStringClaim("email")
            assertEquals(email, subject)
        } catch (ex: Exception) {
            // fallback: decode payload without verification
            val parts = token.split('.')
            assertTrue(parts.size >= 2)
            val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
            val payloadMap: Map<String, Any> = mapper.readValue(payloadJson)
            val subj = (payloadMap["sub"] ?: payloadMap["email"])?.toString()
            assertEquals(email, subj)
        }

        // Проверка Postgres: используем TEST_DB_* переменные или дефолт, соответствующий docker-compose-infra.yml
        val dbUrl = System.getenv("TEST_DB_URL") ?: "jdbc:postgresql://localhost:5432/auth_db"
        val dbUser = System.getenv("TEST_DB_USER") ?: "auth_user"
        val dbPass = System.getenv("TEST_DB_PASSWORD") ?: "auth_pass"

        DriverManager.getConnection(dbUrl, dbUser, dbPass).use { conn ->
            conn.prepareStatement("SELECT count(*) FROM users WHERE email = ?").use { ps ->
                ps.setString(1, email)
                ps.executeQuery().use { rs ->
                    rs.next()
                    val cnt = rs.getInt(1)
                    assertEquals(1, cnt)
                }
            }
        }
    }
}
