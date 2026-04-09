package com.example.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application entry point for the EcoTracker service.
 */
@SpringBootApplication
class Application

/**
 * Boots the Spring application and initializes the web API.
 */
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
