package com.example.api.controller

import org.springframework.stereotype.Controller

/**
 * Implements the page-route contract and forwards friendly URLs to static HTML assets.
 */
@Controller
class LoginPageController : LoginPageControllerApi {

    /**
     * Forwards the browser favicon request to the SVG icon stored in static assets.
     */
    override fun favicon(): String = "forward:/favicon.svg"

    /**
     * Forwards the login route to the login page asset.
     */
    override fun loginPage(): String = "forward:/login.html"

    /**
     * Forwards the register route to the registration page asset.
     */
    override fun registerPage(): String = "forward:/register.html"

    /**
     * Forwards the profile route to the profile page asset.
     */
    override fun profilePage(): String = "forward:/profile.html"

    /**
     * Forwards the user directory route to the users page asset.
     */
    override fun usersPage(): String = "forward:/users.html"
}
