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
     * Returns a user by username ignoring case, or null when no user exists.
     */
    fun findByUsernameIgnoreCase(username: String): AppUser?

    /**
     * Returns a user by email ignoring case, or null when no user exists.
     */
    fun findByEmailIgnoreCase(email: String): AppUser?

    /**
     * Checks whether a username is already taken.
     */
    fun existsByUsername(username: String): Boolean

    /**
     * Checks whether a username is already taken regardless of case.
     */
    fun existsByUsernameIgnoreCase(username: String): Boolean

    /**
     * Checks whether an email address is already attached to a user.
     */
    fun existsByEmailIgnoreCase(email: String): Boolean

    /**
     * Returns all users sorted from oldest to newest registration.
     */
    fun findAllByOrderByCreatedAtAsc(): List<AppUser>
}
