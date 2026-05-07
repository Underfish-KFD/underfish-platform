package ru.underfish.communityservice.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import ru.underfish.communityservice.exception.ForbiddenException
import java.nio.charset.StandardCharsets
import java.util.UUID

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

    fun getRequiredUserUuid(): UUID = toUuid(getRequired().userId)

    fun isCurrentUser(userId: String): Boolean = getRequired().userId == userId

    fun requireSelfOrAdmin(userId: String) {
        if (!isCurrentUser(userId) && !isAdmin()) {
            throw ForbiddenException("Access denied for userId=$userId")
        }
    }

    private fun toUuid(userId: String): UUID =
        runCatching { UUID.fromString(userId) }
            .getOrElse {
                val numericUserId =
                    userId.toLongOrNull()
                        ?: throw IllegalArgumentException("Invalid gateway user id: $userId", it)
                val deterministicUserId = "auth-user:$numericUserId".toByteArray(StandardCharsets.UTF_8)
                UUID.nameUUIDFromBytes(deterministicUserId)
            }
}
