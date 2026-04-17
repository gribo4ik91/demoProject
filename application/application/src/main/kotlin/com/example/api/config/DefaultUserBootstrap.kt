package com.example.api.config

import com.example.api.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Creates an optional default user on startup when explicitly enabled in configuration.
 */
@Configuration
class DefaultUserBootstrap {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun defaultUserInitializer(
        authService: AuthService,
        defaultUserProperties: DefaultUserProperties
    ): ApplicationRunner = ApplicationRunner {
        if (!defaultUserProperties.enabled) {
            logger.info("Default user bootstrap is disabled")
            return@ApplicationRunner
        }

        val created = authService.ensureDefaultUser(
            username = defaultUserProperties.username,
            displayName = defaultUserProperties.displayName,
            firstName = defaultUserProperties.firstName,
            lastName = defaultUserProperties.lastName,
            email = defaultUserProperties.email,
            location = defaultUserProperties.location,
            bio = defaultUserProperties.bio,
            password = defaultUserProperties.password,
            role = defaultUserProperties.role
        )

        if (created) {
            logger.info("Created default user username={}", defaultUserProperties.username)
        } else {
            logger.info("Skipped default user creation because at least one user already exists")
        }
    }
}
