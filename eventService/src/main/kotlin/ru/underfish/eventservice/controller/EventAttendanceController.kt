package ru.underfish.eventservice.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.underfish.eventservice.dto.request.EventAttendanceRequest
import ru.underfish.eventservice.dto.request.EventAttendanceUpdateRequest
import ru.underfish.eventservice.dto.response.EventAttendanceResponse
import ru.underfish.eventservice.security.CurrentUserProvider
import ru.underfish.eventservice.service.EventAttendanceService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/events/{event_id}/attendance")
class EventAttendanceController(
    private val eventAttendanceService: EventAttendanceService,
    private val currentUserProvider: CurrentUserProvider,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addAttendance(
        @PathVariable("event_id") eventId: UUID,
        @Valid @RequestBody request: EventAttendanceRequest,
    ): EventAttendanceResponse {
        val userId = currentUserProvider.getRequiredUserUuid()
        return eventAttendanceService.addAttendance(eventId, userId, request.status)
    }

    @GetMapping
    fun getAttendances(
        @PathVariable("event_id") eventId: UUID,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<EventAttendanceResponse> = eventAttendanceService.getEventAttendances(eventId, status, page, size)

    @PutMapping("/{user_id}")
    fun updateAttendance(
        @PathVariable("event_id") eventId: UUID,
        @PathVariable("user_id") userId: UUID,
        @Valid @RequestBody request: EventAttendanceUpdateRequest,
    ): EventAttendanceResponse = eventAttendanceService.updateAttendance(eventId, userId, request.status)

    @DeleteMapping("/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeAttendance(
        @PathVariable("event_id") eventId: UUID,
        @PathVariable("user_id") userId: UUID,
    ) {
        eventAttendanceService.removeAttendance(eventId, userId)
    }
}

