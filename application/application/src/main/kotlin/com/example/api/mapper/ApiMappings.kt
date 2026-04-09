package com.example.api.mapper

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.EcosystemLogResponse
import com.example.api.dto.EcosystemResponse
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
