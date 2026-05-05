package ru.underfish.eventservice.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import ru.underfish.eventservice.database.entities.enums.EventStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "events")
class Event(
    @Column(name = "organizer_id", nullable = false)
    var organizerId: UUID,
    @Column(name = "title", nullable = false, length = 255)
    var title: String,
) : AbstractEntity() {
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String = ""

    @Column(name = "start_datetime", nullable = false)
    var startDatetime: LocalDateTime = LocalDateTime.now()

    @Column(name = "end_datetime")
    var endDatetime: LocalDateTime? = null

    @Column(name = "location_id", nullable = false)
    var locationId: UUID = UUID.randomUUID()

    @Column(name = "community_id")
    var communityId: UUID? = null

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String = "RUB"

    @Column(name = "poster_url", columnDefinition = "TEXT")
    var posterUrl: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 20)
    var eventStatus: EventStatus = EventStatus.DRAFT

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "max_participants", nullable = false)
    var maxParticipants: Int = 0

    @Column(name = "is_online", nullable = false)
    var isOnline: Boolean = false

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_tags",
        joinColumns = [JoinColumn(name = "event_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")],
    )
    var tags: MutableSet<Tag> = mutableSetOf()
}
