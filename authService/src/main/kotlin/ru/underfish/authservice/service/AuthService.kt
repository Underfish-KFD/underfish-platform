package ru.underfish.authservice.service

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.underfish.authservice.dto.LoginRequest
import ru.underfish.authservice.dto.RefreshTokenRequest
import ru.underfish.authservice.dto.RegisterRequest
import ru.underfish.authservice.dto.TokenResponse
import ru.underfish.authservice.model.User
import ru.underfish.authservice.repository.UserRepository
import ru.underfish.authservice.security.JwtUtil

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
) {
    fun register(request: RegisterRequest): TokenResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User exists")
        }
        val user = User(email = request.email, passwordHash = passwordEncoder.encode(request.password), firstName = request.name)
        return createTokenResponse(userRepository.save(user))
    }

    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
        if (user == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        }
        return createTokenResponse(user)
    }

    fun refresh(request: RefreshTokenRequest): TokenResponse {
        val claims = runCatching { jwtUtil.parseRefreshToken(request.refreshToken) }
            .getOrElse { throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token") }

        val user = userRepository.findById(jwtUtil.userIdFrom(claims))
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token") }

        if (user.email != claims.subject) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
        }

        return createTokenResponse(user)
    }

    private fun createTokenResponse(user: User): TokenResponse {
        val userId = user.id ?: throw IllegalStateException("Saved user id is not initialized")
        val accessToken = jwtUtil.generateAccessToken(userId, user.email)
        return TokenResponse(
            token = accessToken,
            accessToken = accessToken,
            refreshToken = jwtUtil.generateRefreshToken(userId, user.email),
            expiresIn = jwtUtil.accessTokenExpirationMillis / MILLIS_IN_SECOND,
        )
    }

    companion object {
        private const val MILLIS_IN_SECOND = 1000
    }
}
