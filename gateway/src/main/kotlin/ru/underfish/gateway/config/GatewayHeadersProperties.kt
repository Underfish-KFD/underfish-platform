package ru.underfish.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gateway.identity")
data class GatewayHeadersProperties(
    var internalToken: String = "",
    var userIdHeader: String = "X-User-Id",
    var userRolesHeader: String = "X-User-Roles",
    var internalTokenHeader: String = "X-Internal-Token",
)
