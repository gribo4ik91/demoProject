package com.example.api.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ThreadLocalRandom

/**
 * Adds an optional random delay to dynamic HTTP responses so clients can exercise timeout and retry behavior.
 */
@Component
class ResponseDelayFilter(
    private val responseDelayProperties: ResponseDelayProperties
) : OncePerRequestFilter() {

    /**
     * Skips static assets so page chrome still loads quickly while dynamic routes can be throttled.
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/") ||
            path.startsWith("/webjars/") ||
            path == "/favicon.ico"
    }

    /**
     * Applies the configured delay before delegating to the rest of the filter chain.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        maybeDelay()
        filterChain.doFilter(request, response)
    }

    /**
     * Sleeps for a configured fixed or random interval when response-delay simulation is enabled.
     */
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
