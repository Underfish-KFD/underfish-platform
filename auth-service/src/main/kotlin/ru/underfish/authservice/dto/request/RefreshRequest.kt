package ru.underfish.authservice.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(
    @field:NotBlank val refreshToken: String,
)
