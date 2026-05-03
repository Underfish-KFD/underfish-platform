package ru.underfish.eventservice.dto.response

import ru.underfish.eventservice.database.entities.Event
import java.time.LocalDateTime
import java.util.UUID

data class EventResponse(
    val eventId: UUID,
    val title: String,
    val description: String,
    val startDatetime: LocalDateTime,
    val endDatetime: LocalDateTime?,
    val locationId: UUID,
    val price: Double,
    val currency: String,
    val posterUrl: String?,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val maxParticipants: Int,
    val isOnline: Boolean,
) {
    companion object {
        fun fromEntity(event: Event): EventResponse =
            EventResponse(
                eventId = event.id ?: UUID.randomUUID(),
                title = event.title,
                description = event.description,
                startDatetime = event.startDatetime,
                endDatetime = event.endDatetime,
                locationId = event.locationId,
                price = event.price.toDouble(),
                currency = event.currency,
                posterUrl = event.posterUrl,
                status = event.eventStatus.name,
                createdAt = event.createdAt ?: LocalDateTime.now(),
                updatedAt = event.updatedAt,
                maxParticipants = event.maxParticipants,
                isOnline = event.isOnline,
            )
    }
}

