package ru.underfish.authservice.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID

@Component
class JwtUtil(
    private val rsaKeyProvider: RsaKeyProvider,
    @Value("\${jwt.expiration:86400000}") val accessTokenExpirationMillis: Long,
    @Value("\${jwt.refresh-expiration:2592000000}") val refreshTokenExpirationMillis: Long,
) {
    fun generateToken(userId: Long, email: String): String = generateAccessToken(userId, email)

    fun generateAccessToken(userId: Long, email: String): String =
        generateToken(userId = userId, email = email, tokenType = ACCESS_TOKEN_TYPE, expirationMillis = accessTokenExpirationMillis)

    fun generateRefreshToken(userId: Long, email: String): String =
        generateToken(userId = userId, email = email, tokenType = REFRESH_TOKEN_TYPE, expirationMillis = refreshTokenExpirationMillis)

    fun parseRefreshToken(token: String): Claims {
        val claims = parseClaims(token)
        require(claims[TOKEN_TYPE_CLAIM] == REFRESH_TOKEN_TYPE) { "Not a refresh token" }
        return claims
    }

    fun userIdFrom(claims: Claims): Long {
        val id = claims[USER_ID_CLAIM]
        return when (id) {
            is Number -> id.toLong()
            is String -> id.toLong()
            else -> error("Token does not contain userId")
        }
    }

    private fun generateToken(
        userId: Long,
        email: String,
        tokenType: String,
        expirationMillis: Long,
    ): String =
        Jwts.builder()
            .subject(email)
            .claim(USER_ID_CLAIM, userId)
            .claim("user_id", userUuid(userId).toString())
            .claim("roles", listOf(DEFAULT_USER_ROLE))
            .claim("scope", DEFAULT_USER_ROLE)
            .claim(TOKEN_TYPE_CLAIM, tokenType)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMillis))
            .header()
            .keyId(rsaKeyProvider.keyId)
            .and()
            .signWith(rsaKeyProvider.privateKey, Jwts.SIG.RS256)
            .compact()

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(rsaKeyProvider.publicKey)
            .build()
            .parseSignedClaims(token)
            .payload

    private fun userUuid(userId: Long): UUID =
        UUID.nameUUIDFromBytes("auth-user:$userId".toByteArray(StandardCharsets.UTF_8))

    companion object {
        private const val DEFAULT_USER_ROLE = "USER"
        private const val ACCESS_TOKEN_TYPE = "access"
        private const val REFRESH_TOKEN_TYPE = "refresh"
        private const val TOKEN_TYPE_CLAIM = "token_type"
        private const val USER_ID_CLAIM = "userId"
    }
}
