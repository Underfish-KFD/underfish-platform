package ru.underfish.authservice.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.underfish.authservice.dto.RegisterRequest
import ru.underfish.authservice.model.User
import ru.underfish.authservice.repository.UserRepository
import ru.underfish.authservice.security.JwtUtil

@Service
class AuthService(
	private val userRepository: UserRepository,
	private val passwordEncoder: PasswordEncoder,
	private val jwtUtil: JwtUtil,
) {
	fun register(request: RegisterRequest): String {
		if (userRepository.existsByEmail(request.email)) {
			throw IllegalArgumentException("User exists")
		}
		val user = User(email = request.email, passwordHash = passwordEncoder.encode(request.password), firstName = request.name)
		val saved = userRepository.save(user)
		return jwtUtil.generateToken(saved.id ?: 0L, saved.email)
	}
}


