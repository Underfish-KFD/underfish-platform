package ru.underfish.eventservice.dto.response

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ru.underfish.eventservice.database.entities.Event
import ru.underfish.eventservice.database.entities.Review
import ru.underfish.eventservice.database.entities.enums.EventStatus
import java.time.LocalDateTime
import java.util.UUID

class ReviewResponseTest {
    @Test
    fun `fromEntity maps all fields`() {
        val reviewId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val createdAt = LocalDateTime.of(2026, 5, 4, 12, 0)

        val event = Event(organizerId = UUID.randomUUID(), title = "Event").apply {
            id = eventId
            description = "Description"
            startDatetime = LocalDateTime.of(2026, 5, 20, 18, 0)
            endDatetime = LocalDateTime.of(2026, 5, 20, 20, 0)
            locationId = UUID.randomUUID()
            eventStatus = EventStatus.PUBLISHED
        }

        val review = Review(event = event, userId = userId).apply {
            id = reviewId
            rating = 5
            comment = "Great event"
            this.createdAt = createdAt
        }

        val response = ReviewResponse.fromEntity(review)

        assertEquals(reviewId, response.reviewId)
        assertEquals(userId, response.userId)
        assertEquals(eventId, response.eventId)
        assertEquals(5, response.rating)
        assertEquals("Great event", response.comment)
        assertEquals(createdAt, response.createdAt)
    }

    @Test
    fun `fromEntity generates fallback values when ids are missing`() {
        val event = Event(organizerId = UUID.randomUUID(), title = "Fallback event").apply {
            description = "Description"
            startDatetime = LocalDateTime.of(2026, 5, 20, 18, 0)
            endDatetime = LocalDateTime.of(2026, 5, 20, 20, 0)
            locationId = UUID.randomUUID()
            eventStatus = EventStatus.DRAFT
        }

        val review = Review(event = event, userId = UUID.randomUUID()).apply {
            rating = 4
            comment = "Nice"
            this.createdAt = null
        }

        val response = ReviewResponse.fromEntity(review)

        assertNotNull(response.reviewId)
        assertNotNull(response.eventId)
        assertEquals(4, response.rating)
        assertEquals("Nice", response.comment)
        assertNotNull(response.createdAt)
    }
}

