package com.example.api.mapper

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.EcosystemLogResponse
import com.example.api.dto.EcosystemResponse
import com.example.api.dto.UpdateEcosystemRequest
import com.example.api.dto.UpdateLogRequest
import com.example.api.model.Ecosystem
import com.example.api.model.EcosystemLog

/**
 * Converts an ecosystem creation request into a persistable entity.
 */
fun CreateEcosystemRequest.toEntity(): Ecosystem =
    Ecosystem(
        name = name.trim(),
        type = type.trim(),
        description = description?.trim()?.takeIf { it.isNotEmpty() }
    )

/**
 * Applies an ecosystem update request to an existing entity.
 */
fun UpdateEcosystemRequest.applyTo(existing: Ecosystem): Ecosystem =
    existing.copy(
        name = name.trim(),
        type = type.trim(),
        description = description?.trim()?.takeIf { it.isNotEmpty() }
    )

/**
 * Applies a log update request to an existing entity.
 */
fun UpdateLogRequest.applyTo(existing: EcosystemLog): EcosystemLog =
    existing.copy(
        temperatureC = temperatureC,
        humidityPercent = humidityPercent,
        eventType = eventType.trim().uppercase(),
        notes = notes?.trim()?.takeIf { it.isNotEmpty() }
    )

/**
 * Converts an ecosystem entity into its API response representation.
 */
fun Ecosystem.toResponse(): EcosystemResponse =
    EcosystemResponse(
        id = id,
        name = name,
        type = type,
        description = description,
        createdAt = createdAt
    )

/**
 * Converts an ecosystem log entity into its API response representation.
 */
fun EcosystemLog.toResponse(): EcosystemLogResponse =
    EcosystemLogResponse(
        id = id,
        ecosystemId = ecosystem.id,
        temperatureC = temperatureC,
        humidityPercent = humidityPercent,
        eventType = eventType,
        notes = notes,
        recordedAt = recordedAt
    )
