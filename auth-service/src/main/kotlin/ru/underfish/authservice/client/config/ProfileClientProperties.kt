package ru.underfish.authservice.client.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "services.profile")
data class ProfileClientProperties(
    var internalToken: String = "",
    var internalUserId: String = "system",
    var internalRoles: String = "ADMIN",
    var authSource: String = "auth-service",
)

