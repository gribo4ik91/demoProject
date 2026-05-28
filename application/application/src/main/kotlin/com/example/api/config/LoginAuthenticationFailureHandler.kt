package com.example.api.config

import com.example.api.dto.ValidationPatterns
import com.example.api.repository.AppUserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.stereotype.Component

data class LoginFieldError(
    val field: String,
    val label: String,
    val message: String
)

/**
 * Converts failed form-login attempts into field-level messages for the SSR login page.
 */
@Component
class LoginAuthenticationFailureHandler(
    private val appUserRepository: AppUserRepository
) : AuthenticationFailureHandler {

    private val redirectStrategy = DefaultRedirectStrategy()
    private val usernamePattern = Regex(ValidationPatterns.USERNAME)

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val username = request.getParameter("username")?.trim().orEmpty()
        val password = request.getParameter("password").orEmpty()
        val fieldErrors = validateSubmittedFields(username, password).ifEmpty {
            validateCredentials(username)
        }

        request.session.setAttribute(FIELD_ERRORS_SESSION_ATTRIBUTE, fieldErrors)
        request.session.setAttribute(USERNAME_SESSION_ATTRIBUTE, username)
        redirectStrategy.sendRedirect(request, response, "/login?error")
    }

    private fun validateSubmittedFields(username: String, password: String): List<LoginFieldError> =
        buildList {
            when {
                username.isBlank() -> add(usernameError("Login must not be blank."))
                username.length !in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH ->
                    add(usernameError("Login must be between 3 and 40 characters."))
                !usernamePattern.matches(username) ->
                    add(usernameError("Login may contain lowercase letters, numbers, dots, underscores, and hyphens."))
            }

            when {
                password.isBlank() -> add(passwordError("Password must not be blank."))
                password.length !in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH ->
                    add(passwordError("Password must be between 8 and 72 characters."))
            }
        }

    private fun validateCredentials(username: String): List<LoginFieldError> {
        val accountExists = appUserRepository.existsByUsernameIgnoreCase(username)

        return if (accountExists) {
            listOf(passwordError("Password is incorrect for this login."))
        } else {
            listOf(usernameError("No account was found for this login."))
        }
    }

    private fun usernameError(message: String): LoginFieldError =
        LoginFieldError(field = "username", label = "Login", message = message)

    private fun passwordError(message: String): LoginFieldError =
        LoginFieldError(field = "password", label = "Password", message = message)

    companion object {
        const val FIELD_ERRORS_SESSION_ATTRIBUTE = "LOGIN_FIELD_ERRORS"
        const val USERNAME_SESSION_ATTRIBUTE = "LOGIN_ATTEMPTED_USERNAME"
        private const val USERNAME_MIN_LENGTH = 3
        private const val USERNAME_MAX_LENGTH = 40
        private const val PASSWORD_MIN_LENGTH = 8
        private const val PASSWORD_MAX_LENGTH = 72
    }
}
