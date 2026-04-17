package com.example.api.controller

import com.example.api.dto.AuthStatusResponse
import com.example.api.dto.AuthUserResponse
import com.example.api.dto.RegisterUserRequest
import com.example.api.dto.UpdateUserRoleRequest
import com.example.api.dto.UpdateUserProfileRequest
import com.example.api.dto.UserListItemResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register new users and inspect authentication state.")
/**
 * Declares the HTTP contract for authentication and current-user profile endpoints.
 */
interface AuthControllerApi {
    /**
     * Registers a new user account.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register user", description = "Creates a new user account for login-enabled environments.")
    @ApiResponse(responseCode = "201", description = "User registered")
    fun register(@Valid @RequestBody request: RegisterUserRequest): AuthUserResponse

    /**
     * Returns the profile of the currently authenticated user.
     */
    @GetMapping("/profile")
    @Operation(summary = "Current profile", description = "Returns the current signed-in user profile.")
    fun getProfile(authentication: Authentication): AuthUserResponse

    /**
     * Updates editable profile fields for the currently authenticated user.
     */
    @PutMapping("/profile")
    @Operation(summary = "Update profile", description = "Updates editable fields for the current signed-in user.")
    fun updateProfile(
        authentication: Authentication,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): AuthUserResponse

    /**
     * Returns whether authentication is enabled and whether a user is currently signed in.
     */
    @GetMapping("/status")
    @Operation(summary = "Authentication status", description = "Returns whether auth is enabled and which user is signed in.")
    fun getStatus(authentication: Authentication?): AuthStatusResponse

    /**
     * Returns the user directory visible to authenticated users.
     */
    @GetMapping("/users")
    @Operation(summary = "User directory", description = "Returns all registered users for the signed-in account.")
    fun getUsers(authentication: Authentication): List<UserListItemResponse>

    /**
     * Deletes a user account. Only admins may perform this action.
     */
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user", description = "Deletes a user account. Only admins are allowed to do this.")
    fun deleteUser(authentication: Authentication, @PathVariable userId: UUID)

    /**
     * Changes a user role. Only the super admin may perform this action.
     */
    @PutMapping("/users/{userId}/role")
    @Operation(summary = "Change user role", description = "Promotes or demotes a user. Only the super admin is allowed to do this.")
    fun updateUserRole(
        authentication: Authentication,
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserRoleRequest
    ): AuthUserResponse
}
