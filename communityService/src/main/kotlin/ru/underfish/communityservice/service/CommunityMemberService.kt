package ru.underfish.communityservice.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.underfish.communityservice.database.dao.CommunityMemberRepository
import ru.underfish.communityservice.database.dao.CommunityRepository
import ru.underfish.communityservice.database.entities.Community
import ru.underfish.communityservice.database.entities.CommunityMember
import ru.underfish.communityservice.dto.request.CommunityMemberRequest
import ru.underfish.communityservice.dto.response.CommunityMemberResponse
import ru.underfish.communityservice.exception.ForbiddenException
import ru.underfish.communityservice.exception.NotFoundException
import ru.underfish.communityservice.security.CurrentUserProvider
import java.util.UUID

@Service
class CommunityMemberService(
    private val communityMemberRepository: CommunityMemberRepository,
    private val communityRepository: CommunityRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val communityOrganizerService: CommunityOrganizerService,
    private val authClient: ru.underfish.communityservice.client.AuthClient,
) {
    private val logger = LoggerFactory.getLogger(CommunityMemberService::class.java)

    fun addMember(
        communityId: UUID,
        request: CommunityMemberRequest,
    ): CommunityMemberResponse {
        val currentUser = currentUserProvider.getRequired()
        val currentUserId = UUID.fromString(currentUser.userId)

        // Allow self-add, admin, or organizer to add arbitrary users
        val isAdmin = currentUserProvider.isAdmin()
        val isOrg = isOrganizer(communityId, currentUserId)
        if (request.userId != currentUserId && !isAdmin && !isOrg) {
            throw ForbiddenException("Only admin or organizer can add members")
        }

        // Validate user existence in Auth Service
        try {
            authClient.getUser(request.userId)
        } catch (ex: Exception) {
            logger.warn("Auth user lookup failed for userId=${'$'}{request.userId}", ex)
            throw NotFoundException("User not found: ${'$'}{request.userId}")
        }

        val existing = communityMemberRepository.findByCommunityIdAndUserId(communityId, request.userId)
        if (existing != null) {
            return mapToResponse(existing)
        }

        val member =
            CommunityMember().apply {
                this.communityId = communityId
                this.userId = request.userId
                this.role = request.role ?: "MEMBER"
            }

        val saved = communityMemberRepository.save(member)
        return mapToResponse(saved)
    }

    fun listMembers(communityId: UUID): List<CommunityMemberResponse> {
        return communityMemberRepository.findByCommunityId(communityId).map { mapToResponse(it) }
    }

    // Organizer management is handled in CommunityOrganizerService (separate table)

    private fun isOrganizer(
        communityId: UUID,
        userId: UUID,
    ): Boolean {
        // Check community.organizerId OR dedicated community_organizers table OR membership role
        val community: Community? = communityRepository.findByCommunityId(communityId)
        val membership = communityMemberRepository.findByCommunityIdAndUserId(communityId, userId)
        val isFromCommunity = community != null && community.organizerId == userId
        val isFromOrganizersTable = communityOrganizerService.isOrganizer(communityId, userId)
        val isFromMembership = membership?.role?.equals("ORGANIZER", ignoreCase = true) == true
        return isFromCommunity || isFromOrganizersTable || isFromMembership
    }

    fun removeMember(
        communityId: UUID,
        userId: UUID,
    ) {
        val currentUser = currentUserProvider.getRequired()
        val currentUserId = UUID.fromString(currentUser.userId)

        // Allow self removal or organizer/admin to remove others
        if (userId != currentUserId && !currentUserProvider.isAdmin() && !isOrganizer(communityId, currentUserId)) {
            throw ForbiddenException("Only admin or organizer can remove members")
        }

        val existing =
            communityMemberRepository.findByCommunityIdAndUserId(communityId, userId)
                ?: throw NotFoundException("Member not found in community")

        communityMemberRepository.delete(existing)
    }

    private fun mapToResponse(member: CommunityMember): CommunityMemberResponse =
        CommunityMemberResponse(
            memberId = member.id,
            userId = member.userId,
            communityId = member.communityId,
            role = member.role,
            joinedAt = member.joinedAt,
        )
}
