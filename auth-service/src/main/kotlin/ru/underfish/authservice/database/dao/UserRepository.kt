package ru.underfish.authservice.database.dao

import ru.underfish.authservice.database.entities.User

interface UserRepository : AbstractRepository<User> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?

    fun findUserById(id: Long): User?
}
