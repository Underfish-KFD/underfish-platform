package ru.underfish.authservice.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.underfish.authservice.client.config.ProfileClientConfig
import ru.underfish.authservice.client.dto.ProfileRegisterRequest
import ru.underfish.authservice.dto.response.UserResponse

@FeignClient(
    name = "profile-service",
    url = "\${services.profile.url}",
    configuration = [ProfileClientConfig::class],
)
interface ProfileClient {
    @PutMapping("/api/v1/profiles/register")
    fun registerProfile(
        @RequestBody request: ProfileRegisterRequest,
    ): UserResponse

    @DeleteMapping("/api/v1/profiles/{user_id}")
    fun deleteProfile(
        @PathVariable("user_id") userId: Long,
    )
}
