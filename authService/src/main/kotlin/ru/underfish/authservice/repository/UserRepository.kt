package ru.underfish.authservice.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.underfish.authservice.model.User

interface UserRepository : JpaRepository<User, Long> {
	fun findByEmail(email: String): User?
	fun existsByEmail(email: String): Boolean
}


