package com.example.api

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.slf4j.LoggerFactory

/**
 * Main Spring Boot application entry point for the EcoTracker service.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class Application {
    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun logStartup() {
        logger.info("EcoTracker application is ready and accepting requests")
    }
}

/**
 * Boots the Spring application and initializes the web API.
 */
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
