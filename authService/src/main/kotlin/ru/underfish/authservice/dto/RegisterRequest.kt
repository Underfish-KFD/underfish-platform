package ru.underfish.authservice.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null,
)

