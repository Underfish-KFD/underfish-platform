package ru.underfish.communityservice.dto.request

data class CommunityRequest(
    val name: String,
    val description: String,
    val isPrivate: Boolean = false,
    val coverUrl: String? = null,
)
