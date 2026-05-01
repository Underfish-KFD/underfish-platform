package ru.underfish.authservice.client.dto

data class ProfileCreateRequest(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
)
