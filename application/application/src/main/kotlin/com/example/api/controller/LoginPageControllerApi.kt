package com.example.api.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.ui.Model

/**
 * Declares browser-facing routes for the login page and favicon.
 */
interface LoginPageControllerApi {
    /**
     * Forwards the browser favicon request to the SVG icon stored in static assets.
     */
    @GetMapping("/favicon.ico")
    fun favicon(): String

    /**
     * Renders the login page.
     */
    @GetMapping("/login")
    fun loginPage(
        @RequestParam(required = false) error: String?,
        @RequestParam(required = false) logout: String?,
        @RequestParam(required = false) registered: String?,
        model: Model
    ): String
}
