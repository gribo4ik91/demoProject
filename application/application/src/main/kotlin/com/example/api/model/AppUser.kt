package com.example.api.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity representing an application user that can sign in and manage profile data.
 */
@Entity
@Table(name = "app_user")
class AppUser(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true, length = 40)
    var username: String = "",

    @Column(name = "display_name", nullable = false, length = 60)
    var displayName: String = "",

    @Column(name = "first_name", nullable = false, length = 60)
    var firstName: String = "",

    @Column(name = "last_name", nullable = false, length = 60)
    var lastName: String = "",

    @Column(nullable = false, length = 120)
    var email: String = "",

    @Column(length = 80)
    var location: String? = null,

    @Column(length = 500)
    var bio: String? = null,

    @Column(name = "password_hash", nullable = false, length = 100)
    var passwordHash: String = "",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
