package ru.underfish.profile_service.dto.request

data class UserRegistrationRequest(
    val userId: Long? = null,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
)
