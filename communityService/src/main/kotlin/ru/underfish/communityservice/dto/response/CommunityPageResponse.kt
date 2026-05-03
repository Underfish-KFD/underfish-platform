package ru.underfish.communityservice.dto.response

data class CommunityPageResponse(
    val content: List<CommunityResponse>,
    val meta: PageMeta,
)
