package ru.underfish.eventservice.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

data class EventRequest(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val description: String,
    @field:NotNull
    val startDatetime: LocalDateTime,
    @field:NotNull
    val endDatetime: LocalDateTime,
    @field:NotNull
    val locationId: String,
    val price: Double? = null,
    val currency: String? = null,
    val posterUrl: String? = null,
    val communityId: UUID? = null,
    @field:NotBlank
    val status: String,
    val maxParticipants: Int? = null,
    val isOnline: Boolean? = null,
    val photoUrls: List<String>? = null,
)

