package ru.underfish.authservice.security

import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID

@Component
class JwtUtil(
    private val rsaKeyProvider: RsaKeyProvider,
    @Value("\${jwt.expiration:86400000}") private val expiration: Long,
) {
    fun generateToken(userId: Long, email: String): String =
        Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("user_id", userUuid(userId).toString())
            .claim("roles", listOf(DEFAULT_USER_ROLE))
            .claim("scope", DEFAULT_USER_ROLE)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .header()
            .keyId(rsaKeyProvider.keyId)
            .and()
            .signWith(rsaKeyProvider.privateKey, Jwts.SIG.RS256)
            .compact()

    private fun userUuid(userId: Long): UUID =
        UUID.nameUUIDFromBytes("auth-user:$userId".toByteArray(StandardCharsets.UTF_8))

    companion object {
        private const val DEFAULT_USER_ROLE = "USER"
    }
}

