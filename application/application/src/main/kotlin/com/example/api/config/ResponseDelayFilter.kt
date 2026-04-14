package com.example.api.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ThreadLocalRandom

/**
 * Adds an optional random delay to API responses so clients can exercise timeout and retry behavior.
 */
@Component
class ResponseDelayFilter(
    private val responseDelayProperties: ResponseDelayProperties
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith("/api/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        maybeDelay()
        filterChain.doFilter(request, response)
    }

    private fun maybeDelay() {
        if (!responseDelayProperties.enabled) {
            return
        }

        val minDelay = responseDelayProperties.minMs
        val maxDelay = responseDelayProperties.maxMs
        val delayMs = if (minDelay == maxDelay) minDelay else ThreadLocalRandom.current().nextLong(minDelay, maxDelay + 1)

        if (delayMs > 0) {
            Thread.sleep(delayMs)
        }
    }
}
