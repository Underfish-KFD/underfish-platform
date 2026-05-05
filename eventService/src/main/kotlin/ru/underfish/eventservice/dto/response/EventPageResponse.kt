package ru.underfish.eventservice.dto.response

data class EventPageResponse(
    val content: List<EventResponse>,
    val meta: PageMeta,
)
