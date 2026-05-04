package ru.underfish.eventservice.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.underfish.eventservice.database.dao.EventRepository
import ru.underfish.eventservice.database.dao.ReviewRepository
import ru.underfish.eventservice.database.entities.Event
import ru.underfish.eventservice.database.entities.enums.EventStatus
import ru.underfish.eventservice.dto.request.ReviewRequest
import ru.underfish.eventservice.exception.BadRequestException
import ru.underfish.eventservice.integration.InternalLookupClient
import java.time.LocalDateTime
import java.util.UUID

class ReviewServiceTest {
    private val reviewRepository: ReviewRepository = mock()
    private val eventRepository: EventRepository = mock()
    private val internalLookupClient: InternalLookupClient = mock()

    private val service = ReviewService(reviewRepository, eventRepository, internalLookupClient)

    @Test
    fun `createReview should save review when it does not exist yet`() {
        val eventId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val event = Event(organizerId = UUID.randomUUID(), title = "Kotlin meetup").apply {
            id = eventId
            description = "Event description"
            startDatetime = LocalDateTime.of(2026, 5, 20, 18, 0)
            endDatetime = LocalDateTime.of(2026, 5, 20, 20, 0)
            locationId = UUID.randomUUID()
            eventStatus = EventStatus.PUBLISHED
        }
        val request = ReviewRequest(rating = 5, comment = "Great event")

        whenever(reviewRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false)
        whenever(eventRepository.findById(eventId)).thenReturn(java.util.Optional.of(event))
        whenever(reviewRepository.save(any())).thenAnswer { it.arguments[0] }

        val response = service.createReview(eventId, userId, request)

        assertEquals(userId, response.userId)
        assertEquals(eventId, response.eventId)
        assertEquals(5, response.rating)
        assertEquals("Great event", response.comment)
    }

    @Test
    fun `createReview should reject duplicate review`() {
        val eventId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val request = ReviewRequest(rating = 4, comment = "Already reviewed")

        whenever(reviewRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true)

        assertThrows(BadRequestException::class.java) {
            service.createReview(eventId, userId, request)
        }
    }
}

