package ru.underfish.authservice.client.profile

data class ProfileRegistrationRequest(
    val userId: Long,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String = "",
    val phone: String? = null,
)
