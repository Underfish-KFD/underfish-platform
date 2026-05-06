package ru.underfish.authservice.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class RefreshTokenRequest(
    @JsonAlias("refreshToken")
    val refreshToken: String,
)
