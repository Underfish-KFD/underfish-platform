package ru.underfish.authservice.dto.response

import java.time.LocalDateTime

data class UserResponse(
    val userId: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val registrationDate: LocalDateTime,
    val lastLogin: LocalDateTime? = null,
)

