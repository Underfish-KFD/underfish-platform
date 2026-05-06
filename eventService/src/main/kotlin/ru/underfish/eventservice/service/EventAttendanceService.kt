package ru.underfish.eventservice.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.underfish.eventservice.database.dao.EventAttendanceRepository
import ru.underfish.eventservice.database.dao.EventRepository
import ru.underfish.eventservice.database.entities.EventAttendance
import ru.underfish.eventservice.database.entities.enums.AttendanceStatus
import ru.underfish.eventservice.dto.response.EventAttendanceResponse
import ru.underfish.eventservice.exception.BadRequestException
import ru.underfish.eventservice.exception.NotFoundException
import ru.underfish.eventservice.exception.UnauthorizedException
import ru.underfish.eventservice.integration.InternalLookupClient
import java.util.UUID

@Service
class EventAttendanceService(
    private val eventAttendanceRepository: EventAttendanceRepository,
    private val eventRepository: EventRepository,
    private val internalLookupClient: InternalLookupClient,
) {
    fun getAttendedEventIdsForUser(
        requestedUserId: UUID,
        currentUserId: UUID,
        isAdmin: Boolean,
    ): List<UUID> {
        if (!isAdmin && requestedUserId != currentUserId) {
            throw UnauthorizedException("You can view only your own attendance list")
        }
        return eventAttendanceRepository.findByUserId(requestedUserId).mapNotNull { it.event.id }
    }

    @Transactional
    fun addAttendance(
        eventId: UUID,
        userId: UUID,
        status: String,
    ): EventAttendanceResponse {
        if (eventAttendanceRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw BadRequestException("Attendance already exists")
        }

        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("Event not found") }
        internalLookupClient.ensureUserExists(userId)

        val attendance =
            EventAttendance(event = event, userId = userId).apply {
                this.status = parseAttendanceStatus(status)
            }

        return EventAttendanceResponse.fromEntity(eventAttendanceRepository.save(attendance))
    }

    fun getEventAttendances(
        eventId: UUID,
        status: String?,
        page: Int,
        size: Int,
    ): List<EventAttendanceResponse> {
        if (!eventRepository.existsById(eventId)) {
            throw NotFoundException("Event not found")
        }

        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1))
        val pageData =
            if (status.isNullOrBlank()) {
                eventAttendanceRepository.findByEventId(eventId, pageable)
            } else {
                eventAttendanceRepository.findByEventIdAndStatus(eventId, parseAttendanceStatus(status), pageable)
            }

        return pageData.content.map(EventAttendanceResponse::fromEntity)
    }

    @Transactional
    fun updateAttendance(
        eventId: UUID,
        userId: UUID,
        status: String,
    ): EventAttendanceResponse {
        val attendance =
            eventAttendanceRepository.findByEventIdAndUserId(eventId, userId)
                ?: throw NotFoundException("Attendance not found")

        attendance.status = parseAttendanceStatus(status)
        return EventAttendanceResponse.fromEntity(eventAttendanceRepository.save(attendance))
    }

    @Transactional
    fun removeAttendance(
        eventId: UUID,
        userId: UUID,
    ) {
        val attendance =
            eventAttendanceRepository.findByEventIdAndUserId(eventId, userId)
                ?: throw NotFoundException("Attendance not found")
        eventAttendanceRepository.delete(attendance)
    }

    private fun parseAttendanceStatus(status: String): AttendanceStatus =
        AttendanceStatus.entries.firstOrNull { it.name.equals(status, ignoreCase = true) }
            ?: throw BadRequestException("Invalid attendance status")
}
