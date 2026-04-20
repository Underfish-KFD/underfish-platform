package ru.underfish.profile_service.dto.request

data class UserUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
)

