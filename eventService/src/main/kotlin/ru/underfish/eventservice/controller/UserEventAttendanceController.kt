package ru.underfish.eventservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.underfish.eventservice.security.CurrentUserProvider
import ru.underfish.eventservice.service.EventAttendanceService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/events")
class UserEventAttendanceController(
    private val eventAttendanceService: EventAttendanceService,
    private val currentUserProvider: CurrentUserProvider,
) {
    @GetMapping("/{user_id}/attendence")
    fun getUserAttendence(
        @PathVariable("user_id") userId: UUID,
    ): List<UUID> {
        val current = currentUserProvider.getRequired()
        val currentUserId = currentUserProvider.getRequiredUserUuid()
        val isAdmin = current.roles.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) }
        return eventAttendanceService.getAttendedEventIdsForUser(userId, currentUserId, isAdmin)
    }

    @GetMapping("/me/attendence")
    fun getMyAttendence(): List<UUID> {
        val currentUserId = currentUserProvider.getRequiredUserUuid()
        return eventAttendanceService.getAttendedEventIdsForUser(currentUserId, currentUserId, isAdmin = false)
    }
}
