package ru.underfish.communityservice.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import ru.underfish.communityservice.database.entities.enums.CommunityStatus
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "communities")
class Community : AbstractEntity() {
    @Column(name = "community_id", nullable = false, unique = true)
    var communityId: UUID = UUID.randomUUID()

    @Column(name = "name", nullable = false)
    var name: String = ""

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "organizer_id", nullable = false)
    var organizerId: UUID = UUID.randomUUID()

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false

    @Column(name = "cover_url")
    var coverUrl: String? = null

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: CommunityStatus = CommunityStatus.ACTIVE

    @Column(name = "updated_at")
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
