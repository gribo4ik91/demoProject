package com.example.api.config

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

class ResponseDelayFilterTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(ResponseDelayTestConfiguration::class.java)

    @Test
    fun `delays api requests when feature is enabled`() {
        val filter = ResponseDelayFilter(
            ResponseDelayProperties(
                enabled = true,
                minMs = 40,
                maxMs = 40
            )
        )

        val request = MockHttpServletRequest("GET", "/api/v1/ecosystems")
        val response = MockHttpServletResponse()
        val invoked = AtomicBoolean(false)
        val chain = FilterChain { _, _ -> invoked.set(true) }

        val elapsedMs = measureTimeMillis {
            filter.doFilter(request, response, chain)
        }

        assertTrue(invoked.get())
        assertTrue(elapsedMs >= 30, "Expected at least ~40ms delay, actual: ${elapsedMs}ms")
    }

    @Test
    fun `does not delay non api requests`() {
        val filter = ResponseDelayFilter(
            ResponseDelayProperties(
                enabled = true,
                minMs = 40,
                maxMs = 40
            )
        )

        val request = MockHttpServletRequest("GET", "/login")
        val response = MockHttpServletResponse()
        val invoked = AtomicBoolean(false)
        val chain = FilterChain { _, _ -> invoked.set(true) }

        val elapsedMs = measureTimeMillis {
            filter.doFilter(request, response, chain)
        }

        assertTrue(invoked.get())
        assertTrue(elapsedMs >= 30, "Expected dynamic request delay, actual: ${elapsedMs}ms")
    }

    @Test
    fun `does not delay static asset requests`() {
        val filter = ResponseDelayFilter(
            ResponseDelayProperties(
                enabled = true,
                minMs = 40,
                maxMs = 40
            )
        )

        val request = MockHttpServletRequest("GET", "/css/app.css")
        val response = MockHttpServletResponse()
        val invoked = AtomicBoolean(false)
        val chain = FilterChain { _, _ -> invoked.set(true) }

        val elapsedMs = measureTimeMillis {
            filter.doFilter(request, response, chain)
        }

        assertTrue(invoked.get())
        assertTrue(elapsedMs < 30, "Expected static asset request without synthetic delay, actual: ${elapsedMs}ms")
    }

    @Test
    fun `fails fast when delay range is invalid`() {
        val error = assertThrows(IllegalStateException::class.java) {
            contextRunner
                .withPropertyValues(
                    "app.response-delay.enabled=true",
                    "app.response-delay.min-ms=100",
                    "app.response-delay.max-ms=10"
                )
                .run { context ->
                    assertFalse(context.isRunning)
                }
        }

        assertNotNull(error.cause)
    }
}

@EnableConfigurationProperties(ResponseDelayProperties::class)
private class ResponseDelayTestConfiguration
