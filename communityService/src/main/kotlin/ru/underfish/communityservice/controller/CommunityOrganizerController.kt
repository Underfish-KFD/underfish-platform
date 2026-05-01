package ru.underfish.communityservice.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.underfish.communityservice.dto.request.CommunityOrganizerRequest
import ru.underfish.communityservice.dto.response.CommunityOrganizerResponse
import ru.underfish.communityservice.service.CommunityOrganizerService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/communities/{community_id}/organizers")
class CommunityOrganizerController(
    private val communityOrganizerService: CommunityOrganizerService,
) {
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun addOrganizer(
        @PathVariable("community_id") communityId: UUID,
        @RequestBody request: CommunityOrganizerRequest,
    ): ResponseEntity<CommunityOrganizerResponse> {
        val entity = communityOrganizerService.addOrganizer(communityId, request.userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CommunityOrganizerResponse(
                communityId = entity.communityId,
                userId = entity.userId,
            ),
        )
    }

    @DeleteMapping("/{user_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun removeOrganizer(
        @PathVariable("community_id") communityId: UUID,
        @PathVariable("user_id") userId: UUID,
    ): ResponseEntity<Void> {
        communityOrganizerService.removeOrganizer(communityId, userId)
        return ResponseEntity.noContent().build()
    }
}
