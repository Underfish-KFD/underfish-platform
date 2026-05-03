package ru.underfish.communityservice.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "community_members")
class CommunityMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @Column(name = "community_id", nullable = false)
    var communityId: UUID = UUID.randomUUID()

    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID()

    @Column(name = "role", nullable = false)
    var role: String = "MEMBER"

    @Column(name = "joined_at")
    @CreationTimestamp
    var joinedAt: LocalDateTime = LocalDateTime.now()
}
