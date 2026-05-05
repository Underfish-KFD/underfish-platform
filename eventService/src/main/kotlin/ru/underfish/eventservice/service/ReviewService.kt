package ru.underfish.eventservice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.underfish.eventservice.database.dao.EventRepository
import ru.underfish.eventservice.database.dao.ReviewRepository
import ru.underfish.eventservice.database.entities.Review
import ru.underfish.eventservice.dto.request.ReviewRequest
import ru.underfish.eventservice.dto.response.ReviewResponse
import ru.underfish.eventservice.exception.BadRequestException
import ru.underfish.eventservice.exception.NotFoundException
import ru.underfish.eventservice.integration.InternalLookupClient
import java.util.UUID

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val eventRepository: EventRepository,
    private val internalLookupClient: InternalLookupClient,
) {
    @Transactional
    fun createReview(
        eventId: UUID,
        userId: UUID,
        request: ReviewRequest,
    ): ReviewResponse {
        if (reviewRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw BadRequestException("Review already exists for this user and event")
        }

        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("Event not found") }
        internalLookupClient.ensureUserExists(userId)

        val review =
            Review(event = event, userId = userId).apply {
                rating = request.rating
                comment = request.comment
            }

        return ReviewResponse.fromEntity(reviewRepository.save(review))
    }

    fun getEventReviews(eventId: UUID): List<ReviewResponse> {
        if (!eventRepository.existsById(eventId)) {
            throw NotFoundException("Event not found")
        }
        return reviewRepository.findByEventId(eventId).map(ReviewResponse::fromEntity)
    }

    @Transactional
    fun deleteReview(reviewId: UUID) {
        val review = reviewRepository.findById(reviewId).orElseThrow { NotFoundException("Review not found") }
        reviewRepository.delete(review)
    }
}
