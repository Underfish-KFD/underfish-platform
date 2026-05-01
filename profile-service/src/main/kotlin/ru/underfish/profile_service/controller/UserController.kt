package ru.underfish.profile_service.controller


import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.underfish.profile_service.dto.request.UserLoginRequest
import ru.underfish.profile_service.dto.request.UserRegistrationRequest
import ru.underfish.profile_service.dto.request.UserUpdateRequest
import ru.underfish.profile_service.dto.response.UserLoginResponse
import ru.underfish.profile_service.dto.response.UserResponse
import ru.underfish.profile_service.service.UserService

@RestController
@RequestMapping("/api/v1/profiles")
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(
        @RequestBody request: UserRegistrationRequest,
    ): UserResponse = userService.registerUser(request)
/*
    @PostMapping("/login")
    fun loginUser(
        @RequestBody request: UserLoginRequest,
    ): UserLoginResponse = userService.loginUser(request)
*/
    @GetMapping("/{user_id}")
    fun getUserById(
        @PathVariable("user_id") userId: Long,
    ): UserResponse = userService.getUserById(userId)

    @PutMapping("/{user_id}")
    fun updateUser(
        @PathVariable("user_id") userId: Long,
        @RequestBody request: UserUpdateRequest,
    ): UserResponse = userService.updateUser(userId, request)

    @DeleteMapping("/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @PathVariable("user_id") userId: Long,
    ) {
        userService.deleteUser(userId)
    }
}

