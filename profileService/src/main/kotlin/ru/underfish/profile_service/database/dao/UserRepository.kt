package ru.underfish.profile_service.database.dao

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.underfish.profile_service.database.entities.User

interface UserRepository : ru.underfish.profile_service.database.dao.AbstractRepository<User> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?

    fun findUserById(id: Long): User?


    @Modifying
    @Query(
        value = """
            INSERT INTO users (id, created_at, email, password_hash, phone, first_name, last_name, is_banned, role)
            VALUES (:id, CURRENT_TIMESTAMP, :email, :passwordHash, :phone, :firstName, :lastName, false, 'USER')
        """,
        nativeQuery = true,
    )
    fun insertUserWithId(
        @Param("id") id: Long,
        @Param("email") email: String,
        @Param("passwordHash") passwordHash: String,
        @Param("phone") phone: String?,
        @Param("firstName") firstName: String?,
        @Param("lastName") lastName: String?,
    )

    @Query(
        value = "SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), true)",
        nativeQuery = true,
    )
    fun syncUserIdSequence(): Long
}
