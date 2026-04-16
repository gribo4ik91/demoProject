package com.example.api.controller

import com.example.api.dto.AuthStatusResponse
import com.example.api.dto.AuthUserResponse
import com.example.api.dto.RegisterUserRequest
import com.example.api.dto.UpdateUserProfileRequest
import com.example.api.dto.UserListItemResponse
import com.example.api.repository.AppUserRepository
import com.example.api.service.AuthService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Implements the authentication endpoint contract and delegates requests to [AuthService].
 */
@RestController
class AuthController(
    private val authService: AuthService,
    private val appUserRepository: AppUserRepository,
    @Value("\${app.auth.enabled:false}") private val authEnabled: Boolean
) : AuthControllerApi {

    /**
     * Registers a new user account.
     */
    override fun register(@Valid request: RegisterUserRequest): AuthUserResponse =
        authService.registerUser(request)

    /**
     * Returns the profile of the currently authenticated user.
     */
    override fun getProfile(authentication: Authentication): AuthUserResponse =
        authService.getCurrentUserProfile(authentication.name)

    /**
     * Updates editable profile fields for the currently authenticated user.
     */
    override fun updateProfile(
        authentication: Authentication,
        @Valid request: UpdateUserProfileRequest
    ): AuthUserResponse = authService.updateCurrentUserProfile(authentication.name, request)

    /**
     * Returns whether authentication is enabled and whether a user is currently signed in.
     */
    override fun getStatus(authentication: Authentication?): AuthStatusResponse {
        val principal = authentication?.principal as? UserDetails
        val authenticated = authEnabled &&
            authentication != null &&
            authentication.isAuthenticated &&
            principal != null &&
            principal.username != "anonymousUser"
        val user = if (authenticated) appUserRepository.findByUsername(principal!!.username) else null

        return AuthStatusResponse(
            enabled = authEnabled,
            authenticated = authenticated,
            username = if (authenticated) principal?.username else null,
            displayName = user?.displayName,
            role = user?.role
        )
    }

    /**
     * Returns the directory of all registered accounts for authenticated users.
     */
    override fun getUsers(authentication: Authentication): List<UserListItemResponse> =
        authService.getAllUsers(authentication.name)

    /**
     * Deletes a user account when the current account has admin privileges.
     */
    override fun deleteUser(authentication: Authentication, userId: UUID) {
        authService.deleteUser(authentication.name, userId)
    }
}
