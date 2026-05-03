package ru.underfish.eventservice.dto.response

import ru.underfish.eventservice.database.entities.Review
import java.time.LocalDateTime
import java.util.UUID

data class ReviewResponse(
    val reviewId: UUID,
    val userId: UUID,
    val eventId: UUID,
    val rating: Int,
    val comment: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(review: Review): ReviewResponse =
            ReviewResponse(
                reviewId = review.id ?: UUID.randomUUID(),
                userId = review.userId,
                eventId = review.event.id ?: UUID.randomUUID(),
                rating = review.rating,
                comment = review.comment,
                createdAt = review.createdAt ?: LocalDateTime.now(),
            )
    }
}

