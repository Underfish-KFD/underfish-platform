package ru.underfish.authservice.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.underfish.authservice.dto.request.LoginRequest
import ru.underfish.authservice.dto.request.RegisterRequest
import ru.underfish.authservice.dto.response.UserLoginResponse
import ru.underfish.authservice.dto.response.UserResponse
import ru.underfish.authservice.service.AuthService

@RestController
@RequestMapping("/api/v1/users")
class AuthController(private val authService: AuthService) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): UserResponse = authService.register(request)

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): UserLoginResponse = authService.login(request)
}
