package com.example.api.service

import com.example.api.dto.AuthUserResponse
import com.example.api.dto.RegisterUserRequest
import com.example.api.dto.UpdateUserProfileRequest
import com.example.api.dto.UserListItemResponse
import com.example.api.model.AppUser
import com.example.api.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

/**
 * Handles user registration and profile operations for the authenticated account.
 */
@Service
class AuthService(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    data class ActorSnapshot(
        val user: AppUser?,
        val username: String?,
        val displayName: String?
    )

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

        val assignedRole = if (appUserRepository.count() == 0L) "ADMIN" else "USER"

        val savedUser = appUserRepository.save(
            AppUser(
                displayName = normalizedDisplayName,
                username = normalizedUsername,
                firstName = normalizedFirstName,
                lastName = normalizedLastName,
                email = normalizedEmail,
                location = normalizedLocation,
                bio = normalizedBio,
                role = assignedRole,
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
     * Returns all users for the directory page.
     */
    @Transactional(readOnly = true)
    fun getAllUsers(requestingUsername: String): List<UserListItemResponse> {
        logger.info("Loading user directory requestedBy={}", requestingUsername)
        findUserByUsername(requestingUsername)

        return appUserRepository.findAllByOrderByCreatedAtAsc()
            .map { user ->
                UserListItemResponse(
                    id = user.id ?: error("Expected generated user id"),
                    displayName = user.displayName,
                    username = user.username,
                    role = user.role,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    location = user.location,
                    createdAt = user.createdAt
                )
            }
    }

    /**
     * Deletes a user account when the requester is an admin and not deleting themselves.
     */
    @Transactional
    fun deleteUser(requestingUsername: String, userId: UUID) {
        logger.info("Deleting user requestedBy={} userId={}", requestingUsername, userId)
        val requester = findUserByUsername(requestingUsername)
        requireAdmin(requester)

        val target = appUserRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        if (requester.id == target.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Admins cannot delete their own account")
        }

        appUserRepository.delete(target)
    }

    /**
     * Returns a creator snapshot for the supplied username, or null fields when unavailable.
     */
    @Transactional(readOnly = true)
    fun resolveActorSnapshot(username: String?): ActorSnapshot {
        if (username.isNullOrBlank()) {
            return ActorSnapshot(user = null, username = null, displayName = null)
        }

        val user = appUserRepository.findByUsername(username)
        return ActorSnapshot(
            user = user,
            username = user?.username ?: username,
            displayName = user?.displayName
        )
    }

    /**
     * Returns a snapshot representing a system-generated action.
     */
    fun systemActorSnapshot(): ActorSnapshot =
        ActorSnapshot(user = null, username = "system", displayName = "System")

    /**
     * Loads a user by username or throws a not-found error.
     */
    fun findUserByUsername(username: String): AppUser =
        appUserRepository.findByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    /**
     * Verifies that the provided user has admin access.
     */
    private fun requireAdmin(user: AppUser) {
        if (user.role != "ADMIN") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can manage users")
        }
    }

    /**
     * Converts a user entity into the API response shape used by auth endpoints.
     */
    private fun AppUser.toResponse(): AuthUserResponse =
        AuthUserResponse(
            id = id ?: error("Expected generated user id"),
            displayName = displayName,
            username = username,
            role = role,
            firstName = firstName,
            lastName = lastName,
            email = email,
            location = location,
            bio = bio,
            createdAt = createdAt
        )
}
