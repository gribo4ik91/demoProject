package com.example.api.controller

import com.example.api.dto.EcosystemLogResponse
import com.example.api.dto.LogRequest
import com.example.api.dto.PagedResponse
import com.example.api.dto.UpdateLogRequest
import com.example.api.service.EcosystemLogService
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Implements the ecosystem log endpoint contract and delegates requests to [EcosystemLogService].
 */
@RestController
class EcosystemLogController(
    private val ecosystemLogService: EcosystemLogService
) : EcosystemLogControllerApi {

    /**
     * Creates a new log entry for the selected ecosystem.
     */
    override fun addLog(
        authentication: Authentication?,
        ecosystemId: UUID,
        @Valid request: LogRequest
    ): EcosystemLogResponse = ecosystemLogService.addLog(authentication?.name, ecosystemId, request)

    /**
     * Updates an existing log entry for the selected ecosystem.
     */
    override fun updateLog(
        ecosystemId: UUID,
        logId: UUID,
        @Valid request: UpdateLogRequest
    ): EcosystemLogResponse = ecosystemLogService.updateLog(ecosystemId, logId, request)

    /**
     * Returns a paged list of logs, optionally filtered by event type.
     */
    override fun getLogs(
        ecosystemId: UUID,
        eventType: String?,
        page: Int,
        size: Int
    ): PagedResponse<EcosystemLogResponse> =
        ecosystemLogService.getLogs(ecosystemId, eventType, page, size)
}
