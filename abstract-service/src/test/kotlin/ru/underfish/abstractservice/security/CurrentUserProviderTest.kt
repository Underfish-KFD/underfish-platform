package ru.underfish.abstractservice.security

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import ru.underfish.abstractservice.exception.ForbiddenException

class CurrentUserProviderTest {
    private val currentUserProvider = CurrentUserProvider()

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `requireSelfOrAdmin allows self`() {
        setPrincipal(userId = "user-1", roles = listOf("USER"))

        assertDoesNotThrow {
            currentUserProvider.requireSelfOrAdmin("user-1")
        }
    }

    @Test
    fun `requireSelfOrAdmin allows admin`() {
        setPrincipal(userId = "admin-1", roles = listOf("ADMIN"))

        assertDoesNotThrow {
            currentUserProvider.requireSelfOrAdmin("another-user")
        }
    }

    @Test
    fun `requireSelfOrAdmin denies other user without admin role`() {
        setPrincipal(userId = "user-1", roles = listOf("USER"))

        assertThrows(ForbiddenException::class.java) {
            currentUserProvider.requireSelfOrAdmin("user-2")
        }
    }

    @Test
    fun `hasRole handles ROLE_ prefix`() {
        setPrincipal(userId = "user-1", roles = listOf("ROLE_ADMIN"))

        assertTrue(currentUserProvider.hasRole("ADMIN"))
        assertTrue(currentUserProvider.hasRole("ROLE_ADMIN"))
        assertFalse(currentUserProvider.hasRole("USER"))
    }

    private fun setPrincipal(
        userId: String,
        roles: List<String>,
    ) {
        val principal =
            GatewayPrincipal(
                userId = userId,
                email = null,
                roles = roles,
                authSource = "gateway",
            )
        val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
    }
}
