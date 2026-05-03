package ru.underfish.eventservice.dto.response

import java.util.UUID

data class EventTagResponse(
    val eventId: UUID,
    val tagId: UUID,
)

