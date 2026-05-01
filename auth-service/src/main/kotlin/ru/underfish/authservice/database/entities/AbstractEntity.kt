package ru.underfish.authservice.database.entities

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@MappedSuperclass
abstract class AbstractEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
