package com.example.api.repository

import com.example.api.model.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Provides persistence operations for the inventory audit trail.
 */
interface AuditLogRepository : JpaRepository<AuditLog, UUID>
