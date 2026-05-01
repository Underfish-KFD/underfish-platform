package ru.underfish.authservice.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false) val expiresAt: LocalDateTime,
    @Column(nullable = false, unique = true, length = 36)
    val token: String = UUID.randomUUID().toString(),
    @Column(nullable = false) var revoked: Boolean = false,
) : AbstractEntity()
