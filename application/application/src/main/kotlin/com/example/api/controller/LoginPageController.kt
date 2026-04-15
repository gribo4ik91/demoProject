package com.example.api.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Forwards friendly authentication-related routes to their static HTML pages.
 */
@Controller
class LoginPageController {

    /**
     * Forwards the browser favicon request to the SVG icon stored in static assets.
     */
    @GetMapping("/favicon.ico")
    fun favicon(): String = "forward:/favicon.svg"

    /**
     * Forwards the login route to the login page asset.
     */
    @GetMapping("/login")
    fun loginPage(): String = "forward:/login.html"

    /**
     * Forwards the register route to the registration page asset.
     */
    @GetMapping("/register")
    fun registerPage(): String = "forward:/register.html"

    /**
     * Forwards the profile route to the profile page asset.
     */
    @GetMapping("/profile")
    fun profilePage(): String = "forward:/profile.html"
}
