package ru.underfish.authservice.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.underfish.authservice.dto.request.LogoutRequest
import ru.underfish.authservice.dto.request.RefreshRequest
import ru.underfish.authservice.dto.response.AuthResponse
import ru.underfish.authservice.service.AuthService

@RestController
@RequestMapping("/api/v1/auth")
class TokenController(private val authService: AuthService) {
    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshRequest,
    ): AuthResponse = authService.refresh(request.refreshToken)

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ) {
        authService.logout(request.refreshToken)
    }
}
