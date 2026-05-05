package ru.underfish.eventservice.dto.response

import ru.underfish.eventservice.database.entities.EventAttendance
import java.time.LocalDateTime
import java.util.UUID

data class EventAttendanceResponse(
    val attendanceId: UUID,
    val userId: UUID,
    val eventId: UUID,
    val status: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(attendance: EventAttendance): EventAttendanceResponse =
            EventAttendanceResponse(
                attendanceId = attendance.id ?: UUID.randomUUID(),
                userId = attendance.userId,
                eventId = attendance.event.id ?: UUID.randomUUID(),
                status = attendance.status.name,
                createdAt = attendance.createdAt ?: LocalDateTime.now(),
            )
    }
}
