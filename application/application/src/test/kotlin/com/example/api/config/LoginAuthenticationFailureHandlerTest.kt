package com.example.api.config

import com.example.api.repository.AppUserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import kotlin.test.assertEquals

/**
 * Covers the field-level login error mapping without starting the full Spring context.
 */
class LoginAuthenticationFailureHandlerTest {

    @Test
    fun `stores password field error when password is incorrect`() {
        val userRepository = mock(AppUserRepository::class.java)
        `when`(userRepository.existsByUsernameIgnoreCase("demo-user")).thenReturn(true)
        val handler = LoginAuthenticationFailureHandler(userRepository)
        val request = MockHttpServletRequest("POST", "/login")
        val response = MockHttpServletResponse()

        request.addParameter("username", "demo-user")
        request.addParameter("password", "wrongpass")

        handler.onAuthenticationFailure(request, response, BadCredentialsException("Bad credentials"))

        val session = request.getSession(false) ?: error("Expected login failure session")
        val errors = session
            .getAttribute(LoginAuthenticationFailureHandler.FIELD_ERRORS_SESSION_ATTRIBUTE) as List<*>
        val firstError = errors.first() as LoginFieldError

        assertEquals("/login?error", response.redirectedUrl)
        assertEquals("password", firstError.field)
        assertEquals("Password", firstError.label)
        assertEquals("Password is incorrect for this login.", firstError.message)
        assertEquals(
            "demo-user",
            session.getAttribute(LoginAuthenticationFailureHandler.USERNAME_SESSION_ATTRIBUTE)
        )
    }
}
