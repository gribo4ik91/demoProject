package com.example.api.integration

import com.example.api.repository.AppUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Verifies login-protected behavior and authenticated profile flows.
 */
@SpringBootTest(
    properties = [
        "app.auth.enabled=true"
    ]
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthEnabledIntegrationTest : PostgresIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var appUserRepository: AppUserRepository

    /**
     * Removes existing test users before each authentication scenario.
     */
    @BeforeEach
    fun cleanUsers() {
        appUserRepository.deleteAll()
    }

    @Test
    fun `protected app redirects anonymous users to login page`() {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))
    }

    @Test
    fun `login page stays public when auth is enabled`() {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
    }

    @Test
    fun `register page stays public when auth is enabled`() {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk)
    }

    @Test
    fun `user can register log in and access protected api`() {
        val registrationPayload = """
            {
              "displayName": "Demo Gardener",
              "username": "demo-user",
              "firstName": "Demo",
              "lastName": "Gardener",
              "email": "demo-user@example.com",
              "location": "Chisinau",
              "bio": "Testing profile setup",
              "password": "secret123"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationPayload)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.displayName").value("Demo Gardener"))
            .andExpect(jsonPath("$.username").value("demo-user"))

        val loginResult = mockMvc.perform(
            formLogin("/login")
                .user("demo-user")
                .password("secret123")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
            .andReturn()

        val session = loginResult.request.session as? MockHttpSession
            ?: error("Expected authenticated mock session")

        mockMvc.perform(get("/api/v1/ecosystems").session(session))
            .andExpect(status().isOk)

        mockMvc.perform(get("/api/v1/auth/profile").session(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.displayName").value("Demo Gardener"))
            .andExpect(jsonPath("$.location").value("Chisinau"))

        mockMvc.perform(
            put("/api/v1/auth/profile")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                          "displayName": "Demo Gardener Updated",
                          "firstName": "Demo",
                          "lastName": "Keeper",
                          "email": "demo-user-updated@example.com",
                          "location": "Chisinau Center",
                          "bio": "Updated profile for integration testing"
                        }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.displayName").value("Demo Gardener Updated"))
            .andExpect(jsonPath("$.lastName").value("Keeper"))
            .andExpect(jsonPath("$.location").value("Chisinau Center"))
    }
}
