package ru.underfish.authservice.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.underfish.authservice.dto.LoginRequest
import ru.underfish.authservice.dto.RegisterRequest
import ru.underfish.authservice.dto.TokenResponse
import ru.underfish.authservice.service.AuthService

@RestController
@RequestMapping("/api/v1/users")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody request: RegisterRequest): TokenResponse = authService.register(request)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse = authService.login(request)
}
