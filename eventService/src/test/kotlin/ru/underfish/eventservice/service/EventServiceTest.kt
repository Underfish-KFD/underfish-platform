package ru.underfish.eventservice.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.underfish.eventservice.database.dao.EventRepository
import ru.underfish.eventservice.database.dao.TagRepository
import ru.underfish.eventservice.database.entities.enums.EventStatus
import ru.underfish.eventservice.dto.request.EventRequest
import ru.underfish.eventservice.exception.BadRequestException
import ru.underfish.eventservice.integration.InternalLookupClient
import java.time.LocalDateTime
import java.util.UUID

class EventServiceTest {
    private val eventRepository: EventRepository = mock()
    private val tagRepository: TagRepository = mock()
    private val internalLookupClient: InternalLookupClient = mock()

    private val service = EventService(eventRepository, tagRepository, internalLookupClient)

    @Test
    fun `createEvent should map request to response`() {
        val organizerId = UUID.randomUUID()
        val locationId = UUID.randomUUID()
        val request =
            EventRequest(
                title = "Kotlin meetup",
                description = "A meetup about Kotlin",
                startDatetime = LocalDateTime.of(2026, 5, 20, 18, 0),
                endDatetime = LocalDateTime.of(2026, 5, 20, 20, 0),
                locationId = locationId,
                price = 150.0,
                currency = "RUB",
                posterUrl = "https://example.com/poster.png",
                status = "PUBLISHED",
                maxParticipants = 80,
                isOnline = true,
            )

        whenever(eventRepository.save(any())).thenAnswer { it.arguments[0] }

        val response = service.createEvent(request, organizerId)

        assertEquals("Kotlin meetup", response.title)
        assertEquals("A meetup about Kotlin", response.description)
        assertEquals(locationId, response.locationId)
        assertEquals(150.0, response.price)
        assertEquals("RUB", response.currency)
        assertEquals("PUBLISHED", response.status)
        assertEquals(80, response.maxParticipants)
        assertEquals(true, response.isOnline)
    }

    @Test
    fun `createEvent should reject invalid status`() {
        val organizerId = UUID.randomUUID()
        val request =
            EventRequest(
                title = "Invalid status meetup",
                description = "Bad status",
                startDatetime = LocalDateTime.of(2026, 5, 20, 18, 0),
                endDatetime = LocalDateTime.of(2026, 5, 20, 20, 0),
                locationId = UUID.randomUUID(),
                price = null,
                currency = null,
                posterUrl = null,
                status = "NOT_A_STATUS",
                maxParticipants = null,
                isOnline = null,
            )

        assertThrows(BadRequestException::class.java) {
            service.createEvent(request, organizerId)
        }
    }
}

