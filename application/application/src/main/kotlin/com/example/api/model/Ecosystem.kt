package com.example.api.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity representing a tracked ecosystem and its high-level metadata.
 */
@Entity
@Table(name = "ecosystems")
data class Ecosystem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val type: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
