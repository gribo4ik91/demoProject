package com.example.api.service

import com.example.api.dto.AuthUserResponse
import com.example.api.dto.RegisterUserRequest
import com.example.api.dto.UpdateUserRoleRequest
import com.example.api.dto.UpdateUserProfileRequest
import com.example.api.dto.UserListItemResponse
import com.example.api.model.AppUser
import com.example.api.model.AppUserRoles
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
        val savedUser = createUser(
            username = request.username,
            displayName = request.displayName,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            location = request.location,
            bio = request.bio,
            password = request.password,
            role = defaultRoleForNewUser(),
            failOnDuplicate = true
        )

        return savedUser.toResponse()
    }

    /**
     * Creates a configured default user only when no users exist yet.
     */
    @Transactional
    fun ensureDefaultUser(
        username: String,
        displayName: String,
        firstName: String,
        lastName: String,
        email: String,
        location: String?,
        bio: String?,
        password: String,
        role: String
    ): Boolean {
        if (appUserRepository.count() > 0L) {
            return false
        }

        createUser(
            username = username,
            displayName = displayName,
            firstName = firstName,
            lastName = lastName,
            email = email,
            location = location,
            bio = bio,
            password = password,
            role = if (appUserRepository.count() == 0L) AppUserRoles.SUPER_ADMIN else role,
            failOnDuplicate = false
        )

        return true
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
        val requesterRole = normalizedRole(requester.role)

        val target = appUserRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        if (requester.id == target.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Users cannot delete their own account from the directory")
        }

        if (!canDelete(requesterRole, normalizedRole(target.role))) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, deleteForbiddenMessage(requesterRole))
        }

        appUserRepository.delete(target)
    }

    /**
     * Changes a user's role when the requester has super-admin privileges.
     */
    @Transactional
    fun updateUserRole(requestingUsername: String, userId: UUID, request: UpdateUserRoleRequest): AuthUserResponse {
        logger.info(
            "Updating user role requestedBy={} userId={} targetRole={}",
            requestingUsername,
            userId,
            request.role.trim()
        )

        val requester = findUserByUsername(requestingUsername)
        requireSuperAdmin(requester)

        val target = appUserRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        if (requester.id == target.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Super admin cannot change their own role")
        }

        val targetRole = normalizedManagedRole(request.role)
        if (targetRole == AppUserRoles.SUPER_ADMIN) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Only the first user in the system can be super admin")
        }

        val currentRole = normalizedRole(target.role)
        if (currentRole == AppUserRoles.SUPER_ADMIN) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Super admin role cannot be reassigned")
        }

        target.role = targetRole
        return appUserRepository.save(target).toResponse()
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
    private fun requireSuperAdmin(user: AppUser) {
        if (normalizedRole(user.role) != AppUserRoles.SUPER_ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only super admins can manage admin roles")
        }
    }

    private fun canDelete(requesterRole: String, targetRole: String): Boolean =
        when (requesterRole) {
            AppUserRoles.SUPER_ADMIN -> targetRole == AppUserRoles.ADMIN || targetRole == AppUserRoles.USER
            AppUserRoles.ADMIN -> targetRole == AppUserRoles.USER
            else -> false
        }

    private fun deleteForbiddenMessage(requesterRole: String): String =
        when (requesterRole) {
            AppUserRoles.SUPER_ADMIN -> "Super admin can delete admins and regular users"
            AppUserRoles.ADMIN -> "Admin can delete only regular users"
            else -> "Only admins and super admins can delete users"
        }

    private fun defaultRoleForNewUser(): String =
        if (appUserRepository.count() == 0L) AppUserRoles.SUPER_ADMIN else AppUserRoles.USER

    private fun normalizedRole(role: String?): String =
        role?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: AppUserRoles.USER

    /**
     * Normalizes and persists a user while optionally protecting against duplicate usernames.
     */
    private fun createUser(
        username: String,
        displayName: String,
        firstName: String,
        lastName: String,
        email: String,
        location: String?,
        bio: String?,
        password: String,
        role: String,
        failOnDuplicate: Boolean
    ): AppUser {
        val normalizedUsername = username.trim()
        val normalizedDisplayName = displayName.trim()
        val normalizedFirstName = firstName.trim()
        val normalizedLastName = lastName.trim()
        val normalizedEmail = email.trim().lowercase()
        val normalizedLocation = location?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedBio = bio?.trim()?.takeIf { it.isNotEmpty() }

        if (appUserRepository.existsByUsername(normalizedUsername)) {
            if (failOnDuplicate) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Username already exists")
            }

            return appUserRepository.findByUsername(normalizedUsername)
                ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Username already exists")
        }

        return appUserRepository.save(
            AppUser(
                displayName = normalizedDisplayName,
                username = normalizedUsername,
                firstName = normalizedFirstName,
                lastName = normalizedLastName,
                email = normalizedEmail,
                location = normalizedLocation,
                bio = normalizedBio,
                role = normalizedManagedRole(role),
                passwordHash = passwordEncoder.encode(password)
                    ?: error("Expected encoded password")
            )
        )
    }

    private fun normalizedManagedRole(role: String?): String {
        val normalized = role?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be one of SUPER_ADMIN, ADMIN, or USER")
        if (normalized !in setOf(AppUserRoles.SUPER_ADMIN, AppUserRoles.ADMIN, AppUserRoles.USER)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be one of SUPER_ADMIN, ADMIN, or USER")
        }
        return normalized
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
