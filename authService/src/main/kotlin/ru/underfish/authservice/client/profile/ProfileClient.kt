package ru.underfish.authservice.client.profile

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "profile-service",
    url = "\${services.profile.url}",
)
interface ProfileClient {
    @PutMapping("/api/v1/profiles/register")
    fun registerProfile(@RequestBody request: ProfileRegistrationRequest)
}
