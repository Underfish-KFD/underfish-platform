package ru.underfish.profile_service.dto.request

data class UserLoginRequest(
    val email: String,
    val password: String,
)

