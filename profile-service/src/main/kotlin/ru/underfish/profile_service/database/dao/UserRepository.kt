package ru.underfish.profile_service.database.dao


import ru.underfish.profile_service.database.entities.User

interface UserRepository : ru.underfish.profile_service.database.dao.AbstractRepository<User> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?

    fun findUserById(id: Long): User?
}

