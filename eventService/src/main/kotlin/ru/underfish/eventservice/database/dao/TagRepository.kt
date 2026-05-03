package ru.underfish.eventservice.database.dao

import org.springframework.data.jpa.repository.JpaRepository
import ru.underfish.eventservice.database.entities.Tag
import java.util.UUID

interface TagRepository : JpaRepository<Tag, UUID> {
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(name: String, id: UUID): Boolean
}

