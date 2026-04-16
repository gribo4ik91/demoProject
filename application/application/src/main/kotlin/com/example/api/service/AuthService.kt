package com.example.api.service

import com.example.api.dto.AuthUserResponse
import com.example.api.dto.RegisterUserRequest
import com.example.api.dto.UpdateUserProfileRequest
import com.example.api.model.AppUser
import com.example.api.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

/**
 * Handles user registration and profile operations for the authenticated account.
 */
@Service
class AuthService(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Registers a new user after normalizing fields and hashing the password.
     */
    @Transactional
    fun registerUser(request: RegisterUserRequest): AuthUserResponse {
        logger.info("Registering new user username={}", request.username.trim())
        val normalizedUsername = request.username.trim()
        val normalizedDisplayName = request.displayName.trim()
        val normalizedFirstName = request.firstName.trim()
        val normalizedLastName = request.lastName.trim()
        val normalizedEmail = request.email.trim().lowercase()
        val normalizedLocation = request.location?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedBio = request.bio?.trim()?.takeIf { it.isNotEmpty() }

        if (appUserRepository.existsByUsername(normalizedUsername)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Username already exists")
        }

        val savedUser = appUserRepository.save(
            AppUser(
                displayName = normalizedDisplayName,
                username = normalizedUsername,
                firstName = normalizedFirstName,
                lastName = normalizedLastName,
                email = normalizedEmail,
                location = normalizedLocation,
                bio = normalizedBio,
                passwordHash = passwordEncoder.encode(request.password)
                    ?: error("Expected encoded password")
            )
        )

        return savedUser.toResponse()
    }

    /**
     * Returns the current user profile for the supplied username.
     */
    @Transactional(readOnly = true)
    fun getCurrentUserProfile(username: String): AuthUserResponse {
        logger.info("Loading user profile username={}", username)
        return findUserByUsername(username).toResponse()
    }

    /**
     * Updates the current user profile with normalized request values.
     */
    @Transactional
    fun updateCurrentUserProfile(username: String, request: UpdateUserProfileRequest): AuthUserResponse {
        logger.info("Updating user profile username={}", username)
        val user = findUserByUsername(username)

        user.displayName = request.displayName.trim()
        user.firstName = request.firstName.trim()
        user.lastName = request.lastName.trim()
        user.email = request.email.trim().lowercase()
        user.location = request.location?.trim()?.takeIf { it.isNotEmpty() }
        user.bio = request.bio?.trim()?.takeIf { it.isNotEmpty() }

        return appUserRepository.save(user).toResponse()
    }

    /**
     * Loads a user by username or throws a not-found error.
     */
    private fun findUserByUsername(username: String): AppUser =
        appUserRepository.findByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    /**
     * Converts a user entity into the API response shape used by auth endpoints.
     */
    private fun AppUser.toResponse(): AuthUserResponse =
        AuthUserResponse(
            id = id ?: error("Expected generated user id"),
            displayName = displayName,
            username = username,
            firstName = firstName,
            lastName = lastName,
            email = email,
            location = location,
            bio = bio,
            createdAt = createdAt
        )
}
