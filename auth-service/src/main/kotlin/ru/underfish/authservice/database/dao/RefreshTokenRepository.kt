package ru.underfish.authservice.database.dao

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import ru.underfish.authservice.database.entities.RefreshToken
import ru.underfish.authservice.database.entities.User

interface RefreshTokenRepository : AbstractRepository<RefreshToken> {
    fun findByToken(token: String): RefreshToken?

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    fun revokeAllByUser(
        @Param("user") user: User,
    )
}
