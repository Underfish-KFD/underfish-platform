package ru.underfish.authservice.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.underfish.authservice.client.ProfileClient
import ru.underfish.authservice.client.dto.ProfileRegisterRequest
import ru.underfish.authservice.database.dao.RefreshTokenRepository
import ru.underfish.authservice.database.dao.UserRepository
import ru.underfish.authservice.database.entities.RefreshToken
import ru.underfish.authservice.database.entities.User
import ru.underfish.authservice.dto.request.LoginRequest
import ru.underfish.authservice.dto.request.RegisterRequest
import ru.underfish.authservice.dto.response.AuthResponse
import ru.underfish.authservice.dto.response.UserLoginResponse
import ru.underfish.authservice.dto.response.UserResponse
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
    private val profileClient: ProfileClient,
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun register(request: RegisterRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BadRequestException("Email already registered")
        }
        val profileResponse =
            profileClient.registerProfile(
                ProfileRegisterRequest(
                    email = request.email,
                    password = request.password,
                    firstName = request.firstName ?: "",
                    lastName = request.lastName ?: "",
                    phone = request.phone,
                ),
            )
        try {
            userRepository.save(
                User(
                    email = request.email,
                    passwordHash = passwordEncoder.encode(request.password),
                ),
            )
        } catch (ex: DataIntegrityViolationException) {
            val profileId = profileResponse.userId.toLongOrNull()
            if (profileId != null) {
                try {
                    profileClient.deleteProfile(profileId)
                } catch (ex: Exception) {
                    logger.warn("Failed to rollback profile creation for userId=$profileId", ex)
                }
            }
            throw BadRequestException("Email already registered")
        }
        return profileResponse
    }

    fun login(request: LoginRequest): UserLoginResponse {
        val user =
            userRepository.findByEmail(request.email)
                ?: throw UnauthorizedException("Invalid credentials")
        validateCredentials(user, request.password)
        return UserLoginResponse(token = issueAccessToken(user))
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

    private fun issueAccessToken(user: User): String = jwtTokenUtil.generateAccessToken(user.id, user.email, user.role)

    companion object {
        private const val REFRESH_TOKEN_TTL_DAYS = 30L
    }
}
