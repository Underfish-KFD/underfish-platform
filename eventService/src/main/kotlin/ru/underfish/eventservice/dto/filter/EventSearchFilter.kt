package ru.underfish.eventservice.dto.filter

import java.time.LocalDateTime
import java.util.UUID

data class EventSearchFilter(
    val title: String? = null,
    val status: String? = null,
    val isOnline: Boolean? = null,
    val startFrom: LocalDateTime? = null,
    val startTo: LocalDateTime? = null,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val locationId: UUID? = null,
    val tagIds: List<UUID> = emptyList(),
    val communityId: UUID? = null,
    val organizerId: UUID? = null,
)
