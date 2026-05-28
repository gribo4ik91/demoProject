package com.example.api.controller

import com.example.api.config.LoginAuthenticationFailureHandler
import com.example.api.config.LoginFieldError
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model

/**
 * Renders the login page and keeps the favicon route stable.
 */
@Controller
class LoginPageController : LoginPageControllerApi {

    /**
     * Forwards the browser favicon request to the SVG icon stored in static assets.
     */
    override fun favicon(): String = "forward:/favicon.svg"

    /**
     * Renders the login page template.
     */
    override fun loginPage(
        error: String?,
        logout: String?,
        registered: String?,
        request: HttpServletRequest,
        model: Model
    ): String {
        val loginError = !error.isNullOrBlank()
        val session = request.getSession(false)
        val fieldErrors = session
            ?.getAttribute(LoginAuthenticationFailureHandler.FIELD_ERRORS_SESSION_ATTRIBUTE)
            ?.let { it as? List<*> }
            ?.filterIsInstance<LoginFieldError>()
            .orEmpty()
        val attemptedUsername = session
            ?.getAttribute(LoginAuthenticationFailureHandler.USERNAME_SESSION_ATTRIBUTE)
            ?.toString()
            .orEmpty()

        session?.removeAttribute(LoginAuthenticationFailureHandler.FIELD_ERRORS_SESSION_ATTRIBUTE)
        session?.removeAttribute(LoginAuthenticationFailureHandler.USERNAME_SESSION_ATTRIBUTE)

        model.addAttribute("loginError", loginError)
        model.addAttribute("loginFieldErrors", fieldErrors)
        model.addAttribute("loginUsernameError", fieldErrors.firstOrNull { it.field == "username" }?.message.orEmpty())
        model.addAttribute("loginPasswordError", fieldErrors.firstOrNull { it.field == "password" }?.message.orEmpty())
        model.addAttribute("loginUsername", if (loginError) attemptedUsername else registered?.trim().orEmpty())
        model.addAttribute("loggedOut", !logout.isNullOrBlank())
        model.addAttribute("registeredUser", registered?.trim().orEmpty())
        return "pages/login"
    }
}
