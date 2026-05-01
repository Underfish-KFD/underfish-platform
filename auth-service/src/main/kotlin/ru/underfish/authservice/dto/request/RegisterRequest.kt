package ru.underfish.authservice.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 8, message = "Password must be at least 8 characters") val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
)
