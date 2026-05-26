package com.example.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Carries the editable fields for updating the current user profile.
 */
data class UpdateUserProfileRequest(
    @field:NotBlank(message = "Display name must not be blank")
    @field:Size(min = 3, max = 60, message = "Display name must be between 3 and 60 characters")
    @field:Pattern(
        regexp = ValidationPatterns.DISPLAY_NAME,
        message = "Display name may contain letters, numbers, spaces, apostrophes, dots, and hyphens"
    )
    val displayName: String,

    @field:NotBlank(message = "First name must not be blank")
    @field:Size(min = 2, max = 60, message = "First name must be between 2 and 60 characters")
    @field:Pattern(
        regexp = ValidationPatterns.PERSON_NAME,
        message = "First name may contain letters, spaces, apostrophes, dots, and hyphens"
    )
    val firstName: String,

    @field:NotBlank(message = "Last name must not be blank")
    @field:Size(min = 2, max = 60, message = "Last name must be between 2 and 60 characters")
    @field:Pattern(
        regexp = ValidationPatterns.PERSON_NAME,
        message = "Last name may contain letters, spaces, apostrophes, dots, and hyphens"
    )
    val lastName: String,

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid address")
    @field:Size(max = 120, message = "Email must be 120 characters or fewer")
    val email: String,

    @field:Size(max = 80, message = "Location must be 80 characters or fewer")
    @field:Pattern(
        regexp = ValidationPatterns.LOCATION,
        message = "Location may contain letters, numbers, spaces, commas, apostrophes, dots, and hyphens"
    )
    val location: String? = null,

    @field:Size(max = 500, message = "Bio must be 500 characters or fewer")
    val bio: String? = null
)
