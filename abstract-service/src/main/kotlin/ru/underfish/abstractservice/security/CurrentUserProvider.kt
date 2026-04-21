package ru.underfish.abstractservice.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider {
    fun getOrNull(): GatewayPrincipal? =
        SecurityContextHolder.getContext().authentication?.principal as? GatewayPrincipal

    fun getRequired(): GatewayPrincipal =
        getOrNull() ?: throw IllegalStateException("Authenticated principal is not available")
}

