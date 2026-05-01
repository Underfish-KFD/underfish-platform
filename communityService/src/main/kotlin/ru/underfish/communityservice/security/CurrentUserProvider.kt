package ru.underfish.communityservice.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import ru.underfish.communityservice.exception.ForbiddenException

@Component
class CurrentUserProvider {
    fun getOrNull(): GatewayPrincipal? =
        SecurityContextHolder.getContext().authentication?.principal as? GatewayPrincipal

    fun getRequired(): GatewayPrincipal {
        return check(getOrNull() != null) {
            "Authenticated principal is not available"
        }.run { getOrNull()!! }
    }

    fun hasRole(role: String): Boolean {
        val normalized = role.removePrefix("ROLE_")
        return getRequired().roles.any { it.removePrefix("ROLE_").equals(normalized, ignoreCase = true) }
    }

    fun isAdmin(): Boolean = hasRole("ADMIN")

    fun isCurrentUser(userId: String): Boolean = getRequired().userId == userId

    fun requireSelfOrAdmin(userId: String) {
        if (!isCurrentUser(userId) && !isAdmin()) {
            throw ForbiddenException("Access denied for userId=$userId")
        }
    }
}
