package ru.underfish.communityservice.database.dao

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.underfish.communityservice.database.entities.Community
import java.util.UUID

@Repository
interface CommunityRepository : JpaRepository<Community, Long> {
    fun findByCommunityId(communityId: UUID): Community?

    fun findByOrganizerId(organizerId: UUID): List<Community>
}
