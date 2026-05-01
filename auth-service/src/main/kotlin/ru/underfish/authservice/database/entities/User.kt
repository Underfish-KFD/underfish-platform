package ru.underfish.authservice.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import ru.underfish.authservice.database.entities.enums.Role

@Entity
@Table(name = "users")
data class User(
    @Column(nullable = false, unique = true, length = 255) val email: String,
    @Column(nullable = false, length = 255) val passwordHash: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'USER'") val role: Role = Role.USER,
    @Column(nullable = false) var isBanned: Boolean = false,
) : AbstractEntity()
