package ru.underfish.communityservice.database.dao

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.underfish.communityservice.database.entities.CommunityOrganizer
import java.util.UUID

@Repository
interface CommunityOrganizerRepository : JpaRepository<CommunityOrganizer, Long> {
    fun findByCommunityId(communityId: UUID): List<CommunityOrganizer>

    fun findByCommunityIdAndUserId(
        communityId: UUID,
        userId: UUID,
    ): CommunityOrganizer?
}
