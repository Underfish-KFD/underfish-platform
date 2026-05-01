package ru.underfish.communityservice.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class CommunityMemberResponse(
    val memberId: Long,
    val userId: UUID,
    val communityId: UUID,
    val role: String,
    val joinedAt: LocalDateTime,
)
