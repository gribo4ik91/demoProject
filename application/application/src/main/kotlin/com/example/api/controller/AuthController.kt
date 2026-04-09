package com.example.api.controller

import com.example.api.dto.AuthStatusResponse
import com.example.api.dto.AuthUserResponse
import com.example.api.dto.RegisterUserRequest
import com.example.api.dto.UpdateUserProfileRequest
import com.example.api.repository.AppUserRepository
import com.example.api.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PutMapping

/**
 * Exposes authentication and current-user profile endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register new users and inspect authentication state.")
class AuthController(
    private val authService: AuthService,
    private val appUserRepository: AppUserRepository,
    @Value("\${app.auth.enabled:false}") private val authEnabled: Boolean
) {

    /**
     * Registers a new user account.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register user", description = "Creates a new user account for login-enabled environments.")
    @ApiResponse(responseCode = "201", description = "User registered")
    fun register(@Valid @RequestBody request: RegisterUserRequest): AuthUserResponse =
        authService.registerUser(request)

    /**
     * Returns the profile of the currently authenticated user.
     */
    @GetMapping("/profile")
    @Operation(summary = "Current profile", description = "Returns the current signed-in user profile.")
    fun getProfile(authentication: Authentication): AuthUserResponse =
        authService.getCurrentUserProfile(authentication.name)

    /**
     * Updates editable profile fields for the currently authenticated user.
     */
    @PutMapping("/profile")
    @Operation(summary = "Update profile", description = "Updates editable fields for the current signed-in user.")
    fun updateProfile(
        authentication: Authentication,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): AuthUserResponse = authService.updateCurrentUserProfile(authentication.name, request)

    /**
     * Returns whether authentication is enabled and whether a user is currently signed in.
     */
    @GetMapping("/status")
    @Operation(summary = "Authentication status", description = "Returns whether auth is enabled and which user is signed in.")
    fun getStatus(authentication: Authentication?): AuthStatusResponse {
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
            displayName = user?.displayName
        )
    }
}
