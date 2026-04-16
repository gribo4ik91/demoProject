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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

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
            .andExpect(jsonPath("$.role").value("ADMIN"))

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

        mockMvc.perform(get("/api/v1/auth/status").session(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.role").value("ADMIN"))

        mockMvc.perform(get("/api/v1/auth/profile").session(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.displayName").value("Demo Gardener"))
            .andExpect(jsonPath("$.location").value("Chisinau"))
            .andExpect(jsonPath("$.role").value("ADMIN"))

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

    @Test
    fun `first user becomes admin and only admin can delete other users`() {
        registerUser("admin-user", "Admin Person", "admin@example.com")
        registerUser("member-user", "Member Person", "member@example.com")

        val adminSession = login("admin-user", "secret123")
        val memberSession = login("member-user", "secret123")
        val memberId = appUserRepository.findByUsername("member-user")?.id ?: error("Expected member id")
        val adminId = appUserRepository.findByUsername("admin-user")?.id ?: error("Expected admin id")

        mockMvc.perform(get("/api/v1/auth/users").session(adminSession))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].role").value("ADMIN"))
            .andExpect(jsonPath("$[1].role").value("USER"))

        mockMvc.perform(delete("/api/v1/auth/users/$adminId").session(adminSession))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Admins cannot delete their own account"))

        mockMvc.perform(delete("/api/v1/auth/users/$adminId").session(memberSession))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("Only admins can manage users"))

        mockMvc.perform(delete("/api/v1/auth/users/$memberId").session(adminSession))
            .andExpect(status().isNoContent)

        check(appUserRepository.findByUsername("member-user") == null)
    }

    @Test
    fun `authenticated creations expose creator details`() {
        registerUser("admin-user", "Admin Person", "admin@example.com")
        val session = login("admin-user", "secret123")

        val ecosystemResponse = mockMvc.perform(
            post("/api/v1/ecosystems")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Admin Habitat","type":"FLORARIUM","description":"Created by admin"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.createdByUsername").value("admin-user"))
            .andExpect(jsonPath("$.createdByDisplayName").value("Admin Person"))
            .andReturn()
            .response
            .contentAsString

        val ecosystemId = extractId(ecosystemResponse)

        mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/logs")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"eventType":"OBSERVATION","notes":"Created by admin"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.createdByUsername").value("admin-user"))
            .andExpect(jsonPath("$.createdByDisplayName").value("Admin Person"))

        mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/tasks")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"Admin task","taskType":"INSPECTION"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.createdByUsername").value("admin-user"))
            .andExpect(jsonPath("$.createdByDisplayName").value("Admin Person"))
    }

    private fun registerUser(username: String, displayName: String, email: String) {
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                          "displayName": "$displayName",
                          "username": "$username",
                          "firstName": "${displayName.substringBefore(' ')}",
                          "lastName": "${displayName.substringAfter(' ', "User")}",
                          "email": "$email",
                          "password": "secret123"
                        }
                    """.trimIndent()
                )
        ).andExpect(status().isCreated)
    }

    private fun login(username: String, password: String): MockHttpSession =
        mockMvc.perform(
            formLogin("/login")
                .user(username)
                .password(password)
        )
            .andExpect(status().is3xxRedirection)
            .andReturn()
            .request
            .session as? MockHttpSession
            ?: error("Expected authenticated mock session")

    private fun extractId(responseBody: String): UUID =
        UUID.fromString(
            """"id":"([^"]+)"""".toRegex().find(responseBody)?.groupValues?.get(1)
                ?: error("Expected id in response body")
        )
}
