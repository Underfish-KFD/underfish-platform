package ru.underfish.authservice.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.underfish.authservice.database.dao.RefreshTokenRepository
import ru.underfish.authservice.database.dao.UserRepository
import ru.underfish.authservice.database.entities.RefreshToken
import ru.underfish.authservice.database.entities.User
import ru.underfish.authservice.dto.request.LoginRequest
import ru.underfish.authservice.dto.request.RegisterRequest
import ru.underfish.authservice.dto.response.AuthResponse
import ru.underfish.authservice.exception.BadRequestException
import ru.underfish.authservice.exception.UnauthorizedException
import ru.underfish.authservice.security.JwtTokenUtil
import java.time.LocalDateTime

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenUtil: JwtTokenUtil,
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BadRequestException("Email already registered")
        }
        val user =
            userRepository.save(
                User(
                    email = request.email,
                    passwordHash = passwordEncoder.encode(request.password),
                ),
            )
        // Call ProfileClient.createProfile() here once profile-service exposes an internal
        // registration endpoint that does not require a password field.
        return issueTokenPair(user)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user =
            userRepository.findByEmail(request.email)
                ?: throw UnauthorizedException("Invalid credentials")
        validateCredentials(user, request.password)
        return issueTokenPair(user)
    }

    fun refresh(refreshTokenValue: String): AuthResponse {
        val token =
            refreshTokenRepository.findByToken(refreshTokenValue)
                ?: throw UnauthorizedException("Invalid refresh token")
        validateRefreshToken(token)
        token.revoked = true
        refreshTokenRepository.save(token)
        return issueTokenPair(token.user)
    }

    fun logout(refreshTokenValue: String) {
        refreshTokenRepository.findByToken(refreshTokenValue)?.let {
            it.revoked = true
            refreshTokenRepository.save(it)
        }
    }

    private fun validateCredentials(
        user: User,
        password: String,
    ) {
        if (user.isBanned) throw UnauthorizedException("Account suspended")
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }
    }

    private fun validateRefreshToken(token: RefreshToken) {
        if (token.revoked) {
            // Token reuse detected — likely theft; revoke entire session family
            refreshTokenRepository.revokeAllByUser(token.user)
            throw UnauthorizedException("Refresh token already used")
        }
        if (token.expiresAt.isBefore(LocalDateTime.now())) {
            throw UnauthorizedException("Refresh token expired")
        }
    }

    private fun issueTokenPair(user: User): AuthResponse {
        val accessToken = jwtTokenUtil.generateAccessToken(user.id, user.email, user.role)
        val refreshToken =
            refreshTokenRepository.save(
                RefreshToken(
                    user = user,
                    expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_TTL_DAYS),
                ),
            )
        return AuthResponse(accessToken = accessToken, refreshToken = refreshToken.token)
    }

    companion object {
        private const val REFRESH_TOKEN_TTL_DAYS = 30L
    }
}
