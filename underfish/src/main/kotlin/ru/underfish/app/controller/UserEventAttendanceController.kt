package ru.underfish.app.controller

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.underfish.app.service.EventAttendanceService
import ru.underfish.app.service.UserService

@RestController
@RequestMapping("/api/v1/events")
class UserEventAttendanceController(
    private val eventAttendanceService: EventAttendanceService,
    private val userService: UserService,
) {
    @GetMapping("/{user_id}/attendence")
    fun getUserAttendence(
        @PathVariable("user_id") userId: Long,
        authentication: Authentication,
    ): List<Long> {
        val currentUserId = userService.getUserIdByEmail(authentication.name)
        val isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" }
        return eventAttendanceService.getAttendedEventIdsForUser(userId, currentUserId, isAdmin)
    }

    @GetMapping("/me/attendence")
    fun getMyAttendence(authentication: Authentication): List<Long> {
        val currentUserId = userService.getUserIdByEmail(authentication.name)
        return eventAttendanceService.getAttendedEventIdsForUser(currentUserId, currentUserId, isAdmin = false)
    }
}
