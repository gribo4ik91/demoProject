package com.example.api.controller

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
    override fun loginPage(error: String?, logout: String?, registered: String?, model: Model): String {
        model.addAttribute("loginError", !error.isNullOrBlank())
        model.addAttribute("loggedOut", !logout.isNullOrBlank())
        model.addAttribute("registeredUser", registered?.trim().orEmpty())
        return "pages/login"
    }
}
