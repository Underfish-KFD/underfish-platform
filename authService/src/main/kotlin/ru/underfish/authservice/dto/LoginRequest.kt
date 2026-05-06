package ru.underfish.authservice.dto

data class LoginRequest(
    val email: String,
    val password: String,
)
