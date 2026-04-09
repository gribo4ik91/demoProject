package com.example.api.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity representing a recorded event or measurement for an ecosystem.
 */
@Entity
@Table(name = "logs")
data class EcosystemLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecosystem_id", nullable = false)
    val ecosystem: Ecosystem,

    @Column(name = "temperature_c")
    val temperatureC: Double? = null,

    @Column(name = "humidity_percent")
    val humidityPercent: Int? = null,

    @Column(name = "event_type", nullable = false)
    val eventType: String,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "recorded_at", nullable = false, updatable = false)
    val recordedAt: LocalDateTime = LocalDateTime.now()
)
