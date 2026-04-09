package com.example.api.repository

import com.example.api.model.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Provides persistence operations and lookup helpers for application users.
 */
interface AppUserRepository : JpaRepository<AppUser, UUID> {
    /**
     * Returns a user by username, or null when no user exists.
     */
    fun findByUsername(username: String): AppUser?

    /**
     * Checks whether a username is already taken.
     */
    fun existsByUsername(username: String): Boolean
}
