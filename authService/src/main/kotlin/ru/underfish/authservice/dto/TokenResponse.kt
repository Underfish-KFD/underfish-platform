package ru.underfish.authservice.dto

data class TokenResponse(
    /** Backward-compatible alias for clients that still read `token`. */
    val token: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
)
