package com.example.api.controller

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * Adds shared template helpers and authentication state to every server-rendered page.
 */
@ControllerAdvice(assignableTypes = [UiController::class, LoginPageController::class])
class UiControllerAdvice(
    private val uiSupport: UiSupport
) {

    @ModelAttribute("ui")
    fun ui(): UiSupport = uiSupport

    @ModelAttribute("auth")
    fun auth(authentication: Authentication?) = uiSupport.authStatus(authentication)
}
