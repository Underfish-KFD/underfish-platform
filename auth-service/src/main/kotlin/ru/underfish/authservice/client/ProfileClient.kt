package ru.underfish.authservice.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.underfish.authservice.client.dto.ProfileCreateRequest

// Wire this into AuthService.register() once profile-service exposes an internal endpoint
// that accepts user data without a password (legacy monolith still requires it).
@FeignClient(name = "profile-service", url = "\${services.profile.url}")
interface ProfileClient {
    @PostMapping("/api/v1/profiles/register")
    fun createProfile(
        @RequestBody request: ProfileCreateRequest,
    )
}
