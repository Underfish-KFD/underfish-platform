package ru.underfish.profile_service.service


import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.underfish.profile_service.database.dao.UserRepository
import ru.underfish.profile_service.database.entities.enums.Role
import ru.underfish.profile_service.dto.request.UserLoginRequest
import ru.underfish.profile_service.dto.request.UserRegistrationRequest
import ru.underfish.profile_service.dto.request.UserUpdateRequest
import ru.underfish.profile_service.dto.response.UserLoginResponse
import ru.underfish.profile_service.dto.response.UserResponse
import ru.underfish.profile_service.exception.BadRequestException
import ru.underfish.profile_service.exception.NotFoundException
import ru.underfish.profile_service.exception.UnauthorizedException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun registerUser(request: UserRegistrationRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BadRequestException("User with this email already exists")
        }

        request.userId?.let { userId ->
            if (userRepository.existsById(userId)) {
                throw BadRequestException("User with this id already exists")
            }
            userRepository.insertUserWithId(
                id = userId,
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password),
                phone = request.phone,
                firstName = request.firstName,
                lastName = request.lastName,
            )
            userRepository.syncUserIdSequence()
            return getUserById(userId)
        }

        val user =
            ru.underfish.profile_service.database.entities.User(
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password),
                firstName = request.firstName,
                lastName = request.lastName,
                phone = request.phone,
                role = Role.USER,
            )

        val savedUser = userRepository.save(user)
        return UserResponse.fromEntity(savedUser)
    }
/*
    fun loginUser(request: UserLoginRequest): UserLoginResponse {
        val user = userRepository.findByEmail(request.email) ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        val token = jwtTokenUtil.generateToken(user.id, user.email, user.role)
        return UserLoginResponse(token = token)
    }
*/
    fun getUserById(userId: Long): UserResponse {
        val user = userRepository.findUserById(userId) ?: throw NotFoundException("User not found")
        return UserResponse.fromEntity(user)
    }

    fun updateUser(
        userId: Long,
        request: UserUpdateRequest,
    ): UserResponse {
        val user = userRepository.findUserById(userId) ?: throw NotFoundException("User not found")

        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        request.phone?.let { user.phone = it }
        request.avatarUrl?.let { user.avatarUrl = it }

        val updatedUser = userRepository.save(user)
        return UserResponse.fromEntity(updatedUser)
    }

    fun deleteUser(userId: Long) {
        val user = userRepository.findUserById(userId) ?: throw NotFoundException("User not found")
        userRepository.delete(user)
    }

    fun getUserIdByEmail(email: String): Long {
        val user = userRepository.findByEmail(email) ?: throw NotFoundException("User not found")
        return user.id
    }
}

