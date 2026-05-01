package ru.underfish.communityservice.dto.response

import java.util.UUID

data class CommunityOrganizerResponse(
    val communityId: UUID,
    val userId: UUID,
)
