package ru.underfish.communityservice.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class CommunityResponse(
    val communityId: UUID,
    val name: String,
    val description: String?,
    val organizerId: UUID,
    val isPrivate: Boolean,
    val coverUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: String,
)
