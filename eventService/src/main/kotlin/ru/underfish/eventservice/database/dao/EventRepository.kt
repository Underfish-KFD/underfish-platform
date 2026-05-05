package ru.underfish.eventservice.database.dao

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import ru.underfish.eventservice.database.entities.Event
import java.util.UUID

interface EventRepository : JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event>
