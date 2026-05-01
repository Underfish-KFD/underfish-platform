package ru.underfish.communityservice.service

import org.springframework.stereotype.Service
import ru.underfish.communityservice.database.dao.CommunityOrganizerRepository
import ru.underfish.communityservice.database.entities.CommunityOrganizer
import ru.underfish.communityservice.exception.ForbiddenException
import ru.underfish.communityservice.exception.NotFoundException
import ru.underfish.communityservice.security.CurrentUserProvider
import java.util.UUID

@Service
class CommunityOrganizerService(
    private val communityOrganizerRepository: CommunityOrganizerRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val authClient: ru.underfish.communityservice.client.AuthClient,
) {
    fun addOrganizer(
        communityId: UUID,
        userId: UUID,
    ): CommunityOrganizer {
        val currentUser = currentUserProvider.getRequired()
        val currentUserId = UUID.fromString(currentUser.userId)

        // Allow self (on creation), admin, or existing organizer
        if (userId != currentUserId && !currentUserProvider.isAdmin() && !isOrganizer(communityId, currentUserId)) {
            throw ForbiddenException("Only admin or organizer can add organizers")
        }

        // Validate user exists in Auth Service before adding
        try {
            authClient.getUser(userId)
        } catch (ex: Exception) {
            throw NotFoundException("User not found: ${'$'}userId")
        }

        val existing = communityOrganizerRepository.findByCommunityIdAndUserId(communityId, userId)
        if (existing != null) {
            return existing
        }

        val entity =
            CommunityOrganizer().apply {
                this.communityId = communityId
                this.userId = userId
            }

        return communityOrganizerRepository.save(entity)
    }

    fun removeOrganizer(
        communityId: UUID,
        userId: UUID,
    ) {
        val currentUser = currentUserProvider.getRequired()
        val currentUserId = UUID.fromString(currentUser.userId)

        if (!currentUserProvider.isAdmin() && !isOrganizer(communityId, currentUserId)) {
            throw ForbiddenException("Only admin or organizer can remove organizers")
        }

        val existing =
            communityOrganizerRepository.findByCommunityIdAndUserId(communityId, userId)
                ?: throw NotFoundException("Organizer not found")

        communityOrganizerRepository.delete(existing)
    }

    fun listOrganizers(communityId: UUID): List<CommunityOrganizer> =
        communityOrganizerRepository.findByCommunityId(communityId)

    fun isOrganizer(
        communityId: UUID,
        userId: UUID,
    ): Boolean = communityOrganizerRepository.findByCommunityIdAndUserId(communityId, userId) != null
}
