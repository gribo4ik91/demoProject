package com.example.api

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.slf4j.LoggerFactory

/**
 * Bootstraps the EcoTracker Spring Boot application and enables configuration-properties scanning.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class Application {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Writes a startup log entry once the application context is fully ready to serve requests.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun logStartup() {
        logger.info("EcoTracker application is ready and accepting requests")
    }
}

/**
 * Launches the EcoTracker application from the command line.
 */
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
