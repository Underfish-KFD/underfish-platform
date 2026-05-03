package ru.underfish.communityservice.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.underfish.communityservice.dto.request.CommunityMemberRequest
import ru.underfish.communityservice.dto.response.CommunityMemberResponse
import ru.underfish.communityservice.service.CommunityMemberService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/communities/{community_id}/members")
class CommunityMemberController(
    private val communityMemberService: CommunityMemberService,
) {
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun addMember(
        @PathVariable("community_id") communityId: UUID,
        @RequestBody request: CommunityMemberRequest,
    ): ResponseEntity<CommunityMemberResponse> {
        val response = communityMemberService.addMember(communityId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @PreAuthorize("permitAll")
    fun listMembers(
        @PathVariable("community_id") communityId: UUID,
    ): ResponseEntity<List<CommunityMemberResponse>> {
        val list = communityMemberService.listMembers(communityId)
        return ResponseEntity.ok(list)
    }

    @DeleteMapping("/{user_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun removeMember(
        @PathVariable("community_id") communityId: UUID,
        @PathVariable("user_id") userId: UUID,
    ): ResponseEntity<Void> {
        communityMemberService.removeMember(communityId, userId)
        return ResponseEntity.noContent().build()
    }
}
