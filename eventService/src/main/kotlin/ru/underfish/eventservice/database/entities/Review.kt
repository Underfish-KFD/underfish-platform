package ru.underfish.eventservice.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "reviews")
class Review(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
) : AbstractEntity() {
    @Column(name = "rating", nullable = false)
    var rating: Int = 0

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    var comment: String = ""
}
