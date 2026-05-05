package ru.underfish.eventservice.dto.response

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ru.underfish.eventservice.database.entities.Event
import ru.underfish.eventservice.database.entities.enums.EventStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class EventResponseTest {
    @Test
    fun `fromEntity maps all fields`() {
        val eventId = UUID.randomUUID()
        val organizerId = UUID.randomUUID()
        val locationId = UUID.randomUUID()
        val createdAt = LocalDateTime.of(2026, 5, 4, 10, 15)
        val updatedAt = LocalDateTime.of(2026, 5, 4, 11, 30)

        val event =
            Event(organizerId = organizerId, title = "Kotlin meetup").apply {
                id = eventId
                description = "Event description"
                startDatetime = LocalDateTime.of(2026, 5, 20, 18, 0)
                endDatetime = LocalDateTime.of(2026, 5, 20, 20, 0)
                this.locationId = locationId
                price = BigDecimal("199.90")
                currency = "RUB"
                posterUrl = "https://example.com/poster.png"
                eventStatus = EventStatus.PUBLISHED
                this.createdAt = createdAt
                this.updatedAt = updatedAt
                maxParticipants = 120
                isOnline = true
            }

        val response = EventResponse.fromEntity(event)

        assertEquals(eventId, response.eventId)
        assertEquals("Kotlin meetup", response.title)
        assertEquals("Event description", response.description)
        assertEquals(LocalDateTime.of(2026, 5, 20, 18, 0), response.startDatetime)
        assertEquals(LocalDateTime.of(2026, 5, 20, 20, 0), response.endDatetime)
        assertEquals(locationId, response.locationId)
        assertEquals(199.90, response.price)
        assertEquals("RUB", response.currency)
        assertEquals("https://example.com/poster.png", response.posterUrl)
        assertEquals("PUBLISHED", response.status)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
        assertEquals(120, response.maxParticipants)
        assertEquals(true, response.isOnline)
    }

    @Test
    fun `fromEntity generates fallback values when id and createdAt are missing`() {
        val organizerId = UUID.randomUUID()
        val locationId = UUID.randomUUID()

        val event =
            Event(organizerId = organizerId, title = "Fallback meetup").apply {
                description = "Fallback description"
                startDatetime = LocalDateTime.of(2026, 6, 1, 12, 0)
                endDatetime = null
                this.locationId = locationId
                price = BigDecimal.ZERO
                currency = "RUB"
                posterUrl = null
                eventStatus = EventStatus.DRAFT
                this.createdAt = null
                this.updatedAt = LocalDateTime.of(2026, 6, 1, 12, 30)
                maxParticipants = 0
                isOnline = false
            }

        val response = EventResponse.fromEntity(event)

        assertNotNull(response.eventId)
        assertEquals("Fallback meetup", response.title)
        assertEquals("Fallback description", response.description)
        assertEquals(locationId, response.locationId)
        assertEquals(0.0, response.price)
        assertEquals("RUB", response.currency)
        assertEquals("DRAFT", response.status)
        assertNotNull(response.createdAt)
        assertEquals(LocalDateTime.of(2026, 6, 1, 12, 30), response.updatedAt)
        assertEquals(0, response.maxParticipants)
        assertEquals(false, response.isOnline)
    }
}
