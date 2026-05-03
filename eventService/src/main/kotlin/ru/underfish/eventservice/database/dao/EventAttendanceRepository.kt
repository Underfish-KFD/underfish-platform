package ru.underfish.eventservice.database.dao

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ru.underfish.eventservice.database.entities.EventAttendance
import ru.underfish.eventservice.database.entities.enums.AttendanceStatus
import java.util.UUID

interface EventAttendanceRepository : JpaRepository<EventAttendance, UUID> {
    fun existsByEventIdAndUserId(eventId: UUID, userId: UUID): Boolean

    fun findByEventId(eventId: UUID, pageable: Pageable): Page<EventAttendance>

    fun findByEventIdAndStatus(eventId: UUID, status: AttendanceStatus, pageable: Pageable): Page<EventAttendance>

    fun findByEventIdAndUserId(eventId: UUID, userId: UUID): EventAttendance?
}

