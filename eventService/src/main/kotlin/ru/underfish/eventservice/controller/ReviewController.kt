package ru.underfish.eventservice.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.underfish.eventservice.dto.request.ReviewRequest
import ru.underfish.eventservice.dto.response.ReviewResponse
import ru.underfish.eventservice.security.CurrentUserProvider
import ru.underfish.eventservice.service.ReviewService
import java.util.UUID

@RestController
class ReviewController(
    private val reviewService: ReviewService,
    private val currentUserProvider: CurrentUserProvider,
) {
    @PostMapping("/api/v1/events/{event_id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    fun createReview(
        @PathVariable("event_id") eventId: UUID,
        @Valid @RequestBody request: ReviewRequest,
    ): ReviewResponse {
        val userId = UUID.fromString(currentUserProvider.getRequired().userId)
        return reviewService.createReview(eventId, userId, request)
    }

    @GetMapping("/api/v1/events/{event_id}/reviews")
    fun getEventReviews(
        @PathVariable("event_id") eventId: UUID,
    ): List<ReviewResponse> = reviewService.getEventReviews(eventId)

    @DeleteMapping("/api/v1/reviews/{review_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReview(
        @PathVariable("review_id") reviewId: UUID,
    ) {
        reviewService.deleteReview(reviewId)
    }
}

