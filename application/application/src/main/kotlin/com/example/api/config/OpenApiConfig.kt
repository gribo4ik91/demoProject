package com.example.api.config

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures the OpenAPI metadata exposed through Swagger UI and generated API documentation.
 */
@Configuration
class OpenApiConfig {

    /**
     * Builds the OpenAPI descriptor for the EcoTracker REST API.
     */
    @Bean
    fun ecoTrackerOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("EcoTracker API")
                    .description(
                        "REST API for managing tracked ecosystems and their activity logs. " +
                            "Use it to create ecosystems, record observations, and review history."
                    )
                    .version("v1")
                    .contact(
                        Contact()
                            .name("EcoTracker")
                            .email("maintainer@ecotracker.local")
                    )
                    .license(
                        License()
                            .name("Demo Project")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8085")
                        .description("Local development")
                )
            )
            .externalDocs(
                ExternalDocumentation()
                    .description("See the local project README for setup and architecture details.")
            )
}
