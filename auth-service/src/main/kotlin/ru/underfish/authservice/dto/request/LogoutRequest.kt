package ru.underfish.authservice.dto.request

import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:NotBlank val refreshToken: String,
)
