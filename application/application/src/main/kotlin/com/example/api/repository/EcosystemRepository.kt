package com.example.api.repository

import com.example.api.model.Ecosystem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Provides persistence operations for ecosystem entities.
 */
@Repository
interface EcosystemRepository : JpaRepository<Ecosystem, UUID>
