package com.example.api.config

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Holds the configuration values that control optional simulated API response delays.
 */
@Validated
@ConfigurationProperties(prefix = "app.response-delay")
data class ResponseDelayProperties(
    val enabled: Boolean = false,
    @field:Min(0)
    val minMs: Long = 0,
    @field:Min(0)
    val maxMs: Long = 5000
) {
    /**
     * Ensures the configured delay range remains valid before the application starts serving traffic.
     */
    @AssertTrue(message = "app.response-delay.min-ms must be less than or equal to app.response-delay.max-ms")
    fun isRangeValid(): Boolean = minMs <= maxMs
}
