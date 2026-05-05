package ru.underfish.authservice.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil(@Value("\${jwt.secret:underfish-auth-service-secret-key-must-be-32-bytes-minimum!}") private val secret: String,
              @Value("\${jwt.expiration:86400000}") private val expiration: Long) {
    fun generateToken(userId: Long, email: String): String =
        Jwts.builder()
            .setSubject(email)
            .claim("userId", userId)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, secret.toByteArray())
            .compact()
}

