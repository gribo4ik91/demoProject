package com.example.api.config

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Holds the optional bootstrap user configuration used to pre-create one login on startup.
 */
@Validated
@ConfigurationProperties(prefix = "app.auth.default-user")
data class DefaultUserProperties(
    val enabled: Boolean = false,
    @field:Size(min = 3, max = 40)
    val username: String = "demo_user_auth",
    @field:Size(min = 3, max = 60)
    val displayName: String = "Demo Gardener",
    @field:Size(min = 2, max = 60)
    val firstName: String = "Demo",
    @field:Size(min = 2, max = 60)
    val lastName: String = "Gardener",
    @field:Email
    @field:Size(max = 120)
    val email: String = "demo_user_auth@example.com",
    @field:Size(max = 80)
    val location: String? = "Chisinau",
    @field:Size(max = 500)
    val bio: String? = "Building calm terrarium routines and logging daily ecosystem changes.",
    @field:Size(min = 6, max = 72)
    val password: String = "secret123",
    @field:Size(min = 4, max = 20)
    val role: String = "SUPER_ADMIN"
)
