package ru.underfish.eventservice.database.dao

import org.springframework.data.jpa.repository.JpaRepository
import ru.underfish.eventservice.database.entities.Review
import java.util.UUID

interface ReviewRepository : JpaRepository<Review, UUID> {
    fun existsByEventIdAndUserId(
        eventId: UUID,
        userId: UUID,
    ): Boolean

    fun findByEventId(eventId: UUID): List<Review>
}
