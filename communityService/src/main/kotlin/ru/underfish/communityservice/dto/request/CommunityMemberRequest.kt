package ru.underfish.communityservice.dto.request

import java.util.UUID

data class CommunityMemberRequest(
    val userId: UUID,
    val role: String? = null,
)
