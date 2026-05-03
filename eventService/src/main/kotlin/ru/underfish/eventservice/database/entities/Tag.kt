package ru.underfish.eventservice.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "tags")
class Tag(
    @Column(name = "name", nullable = false, unique = true, length = 100)
    var name: String,
) : AbstractEntity()

