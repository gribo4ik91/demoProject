package com.example.api.controller

import org.springframework.web.bind.annotation.GetMapping

/**
 * Declares the browser-facing routes that forward to static authentication pages.
 */
interface LoginPageControllerApi {
    /**
     * Forwards the browser favicon request to the SVG icon stored in static assets.
     */
    @GetMapping("/favicon.ico")
    fun favicon(): String

    /**
     * Forwards the login route to the login page asset.
     */
    @GetMapping("/login")
    fun loginPage(): String

    /**
     * Forwards the register route to the registration page asset.
     */
    @GetMapping("/register")
    fun registerPage(): String

    /**
     * Forwards the profile route to the profile page asset.
     */
    @GetMapping("/profile")
    fun profilePage(): String

    /**
     * Forwards the user directory route to the users page asset.
     */
    @GetMapping("/users")
    fun usersPage(): String
}
