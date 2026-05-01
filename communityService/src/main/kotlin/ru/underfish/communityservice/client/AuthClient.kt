package ru.underfish.communityservice.client

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Temporary stub for Auth client — Feign integration removed.
 *
 * This stub bypasses remote checks and returns an empty map for any user.
 * Reason: temporarily avoid external dependency; admin is responsible for
 * adding valid users.
 *
 * Replace with a proper Feign client or generated OpenAPI client when
 * re-enabling service-to-service verification.
 */
@Component
class AuthClient {
    /**
     * Stubbed method that previously called GET /api/v1/users/{id} on Auth Service.
     * Returning an empty map so existing callers that only check for non-null
     * or for success continue to work while Feign is removed.
     */
    fun getUser(@Suppress("UNUSED_PARAMETER") id: UUID): Map<String, Any> = emptyMap()
}

