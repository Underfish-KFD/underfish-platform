package ru.underfish.eventservice.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider {
    fun getOrNull(): GatewayPrincipal? =
        SecurityContextHolder.getContext().authentication?.principal as? GatewayPrincipal

    fun getRequired(): GatewayPrincipal {
        return checkNotNull(getOrNull()) { "Authenticated principal is not available" }
    }
}
