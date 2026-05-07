package ru.underfish.authservice.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.underfish.authservice.dto.RefreshTokenRequest
import ru.underfish.authservice.dto.TokenResponse
import ru.underfish.authservice.service.AuthService

@RestController
@RequestMapping("/api/v1/tokens")
class TokenController(
    private val authService: AuthService,
) {
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshTokenRequest): TokenResponse = authService.refresh(request)
}
