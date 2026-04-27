package ru.underfish.communityservice.database.dao

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.underfish.communityservice.database.entities.CommunityMember
import java.util.UUID

@Repository
interface CommunityMemberRepository : JpaRepository<CommunityMember, Long> {
    fun findByCommunityId(communityId: UUID): List<CommunityMember>

    fun findByCommunityIdAndUserId(
        communityId: UUID,
        userId: UUID,
    ): CommunityMember?

    fun findByUserId(userId: UUID): List<CommunityMember>
}
