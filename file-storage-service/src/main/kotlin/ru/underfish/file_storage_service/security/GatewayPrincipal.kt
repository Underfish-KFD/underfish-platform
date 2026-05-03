package ru.underfish.file_storage_service.security


data class GatewayPrincipal(
    val userId: String,
    val email: String?,
    val roles: List<String>,
    val authSource: String?,
)

