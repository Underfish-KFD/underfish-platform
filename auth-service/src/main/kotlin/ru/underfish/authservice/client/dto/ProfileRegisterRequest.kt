package ru.underfish.authservice.client.dto

data class ProfileRegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
)
