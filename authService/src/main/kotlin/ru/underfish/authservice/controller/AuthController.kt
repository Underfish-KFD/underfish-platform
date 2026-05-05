package ru.underfish.authservice.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.underfish.authservice.dto.RegisterRequest
import ru.underfish.authservice.dto.RegisterResponse
import ru.underfish.authservice.service.AuthService

@RestController
@RequestMapping("/api/v1/users")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody request: RegisterRequest): RegisterResponse {
        val token = authService.register(request)
        return RegisterResponse(token = token)
    }
}

