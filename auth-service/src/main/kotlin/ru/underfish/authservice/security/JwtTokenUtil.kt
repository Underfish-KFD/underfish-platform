package ru.underfish.authservice.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import ru.underfish.authservice.database.entities.enums.Role
import java.security.KeyPair
import java.util.Date

@Component
class JwtTokenUtil(private val rsaKeyPair: KeyPair) {
    fun generateAccessToken(
        userId: Long,
        email: String,
        role: Role,
    ): String =
        Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("role", role.name)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + ACCESS_TOKEN_TTL_MS))
            .signWith(rsaKeyPair.private)
            .compact()

    fun getClaims(token: String): Claims =
        Jwts.parser()
            .keyLocator { _ -> rsaKeyPair.public }
            .build()
            .parseSignedClaims(token)
            .payload

    fun validateToken(token: String): Boolean =
        try {
            getClaims(token)
            true
        } catch (_: Exception) {
            false
        }

    fun getEmailFromToken(token: String): String = getClaims(token).subject

    fun getUserIdFromToken(token: String): Long = getClaims(token).get("userId", Number::class.java).toLong()

    fun getRoleFromToken(token: String): Role = Role.valueOf(getClaims(token).get("role", String::class.java))

    companion object {
        const val ACCESS_TOKEN_TTL_MS = 15 * 60 * 1000L
    }
}
