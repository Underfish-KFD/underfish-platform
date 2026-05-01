package ru.underfish.communityservice.dto.response

data class PageMeta(
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
)
