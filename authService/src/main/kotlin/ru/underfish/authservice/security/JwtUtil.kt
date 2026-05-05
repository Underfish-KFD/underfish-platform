package ru.underfish.authservice.security

import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil(
    private val rsaKeyProvider: RsaKeyProvider,
    @Value("\${jwt.expiration:86400000}") private val expiration: Long,
) {
    fun generateToken(userId: Long, email: String): String =
        Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("user_id", userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .header()
            .keyId(rsaKeyProvider.keyId)
            .and()
            .signWith(rsaKeyProvider.privateKey, Jwts.SIG.RS256)
            .compact()
}

