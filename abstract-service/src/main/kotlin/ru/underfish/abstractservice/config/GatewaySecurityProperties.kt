package ru.underfish.abstractservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gateway.security")
data class GatewaySecurityProperties(
    var enabled: Boolean = true,
    var publicPaths: List<String> = listOf("/actuator/health", "/actuator/info"),
    var rolesDelimiter: String = ",",
    var trustedInternalToken: String? = null,
    var headers: Headers = Headers(),
) {
    data class Headers(
        var userId: String = "X-User-Id",
        var userEmail: String = "X-User-Email",
        var userRoles: String = "X-User-Roles",
        var authSource: String = "X-Auth-Source",
        var internalToken: String = "X-Internal-Token",
    )
}
