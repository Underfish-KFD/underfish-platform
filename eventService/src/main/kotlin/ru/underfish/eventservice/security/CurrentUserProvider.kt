package ru.underfish.eventservice.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
class CurrentUserProvider {
    fun getOrNull(): GatewayPrincipal? =
        SecurityContextHolder.getContext().authentication?.principal as? GatewayPrincipal

    fun getRequired(): GatewayPrincipal =
        getOrNull() ?: throw IllegalStateException("Authenticated principal is not available")

    fun getRequiredUserUuid(): UUID = toUuid(getRequired().userId)

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

