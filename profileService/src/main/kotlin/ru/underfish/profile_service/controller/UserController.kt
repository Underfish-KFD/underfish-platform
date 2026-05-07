package ru.underfish.profile_service.controller


import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.underfish.profile_service.dto.request.UserRegistrationRequest
import ru.underfish.profile_service.dto.request.UserUpdateRequest
import ru.underfish.profile_service.dto.response.UserResponse
import ru.underfish.profile_service.exception.ForbiddenException
import ru.underfish.profile_service.security.CurrentUserProvider
import ru.underfish.profile_service.service.UserService

@RestController
@RequestMapping("/api/v1/profiles")
class UserController(
    private val userService: UserService,
    private val currentUserProvider: CurrentUserProvider,
) {

    @PutMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUserPut(
        @RequestBody request: UserRegistrationRequest,
    ): UserResponse {
        if (!currentUserProvider.isAdmin()) {
            throw ForbiddenException("Access denied")
        }
        return userService.registerUser(request)
    }

    @GetMapping("/{user_id}")
    fun getUserById(
        @PathVariable("user_id") userId: Long
    ): UserResponse {

        currentUserProvider.requireSelfOrAdmin(userId.toString())

        return userService.getUserById(userId)
    }

    @PutMapping("/{user_id}")
    fun updateUser(
        @PathVariable("user_id") userId: Long,
        @RequestBody request: UserUpdateRequest
    ): UserResponse {
        currentUserProvider.requireSelfOrAdmin(userId.toString())

        return userService.updateUser(userId, request)
    }
    @DeleteMapping("/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @PathVariable("user_id") userId: Long,
    ) {
        if (!currentUserProvider.isAdmin()) {
            throw ForbiddenException("Access denied")
        }
        userService.deleteUser(userId)
    }
}
