package ru.underfish.authservice.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var email: String = "",
    @Column(nullable = false)
    var passwordHash: String = "",
    var firstName: String? = null,
    var lastName: String? = null,
)

