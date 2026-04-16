package com.example.api.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.Instant

/**
 * Logs incoming HTTP requests and the final response status with execution time.
 */
@Component
@Order(1)
class RequestResponseLoggingFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Skips noisy static-asset requests so application logs stay focused on interactive traffic.
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/") ||
            path.startsWith("/webjars/")
    }

    /**
     * Logs request metadata before delegation and logs the final status after downstream processing completes.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startedAt = Instant.now()
        val method = request.method
        val path = request.requestURI
        val query = request.queryString?.let { "?$it" } ?: ""
        val username = request.userPrincipal?.name ?: "anonymous"
        val clientIp = extractClientIp(request)

        logger.info("HTTP IN  method={} path={}{} user={} ip={}", method, path, query, username, clientIp)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val elapsedMs = Duration.between(startedAt, Instant.now()).toMillis()
            logger.info(
                "HTTP OUT method={} path={}{} status={} durationMs={}",
                method,
                path,
                query,
                response.status,
                elapsedMs
            )
        }
    }

    /**
     * Resolves the best available client IP, preferring the first forwarded address when present.
     */
    private fun extractClientIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: request.remoteAddr
}
