package com.example.api.service

import com.example.api.dto.RegisterUserRequest
import com.example.api.repository.AppUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException

/**
 * Covers duplicate protection for user registration.
 */
class AuthServiceTest {

    private lateinit var appUserRepository: AppUserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        appUserRepository = Mockito.mock(AppUserRepository::class.java)
        passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
        authService = AuthService(appUserRepository, passwordEncoder)
        Mockito.`when`(appUserRepository.count()).thenReturn(1L)
    }

    @Test
    fun `register user rejects duplicate login`() {
        Mockito.`when`(appUserRepository.existsByUsernameIgnoreCase("demo-user")).thenReturn(true)

        val exception = assertThrows(ResponseStatusException::class.java) {
            authService.registerUser(validRegistration())
        }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
        assertEquals("Username already exists", exception.reason)
    }

    @Test
    fun `register user rejects duplicate email`() {
        Mockito.`when`(appUserRepository.existsByUsernameIgnoreCase("demo-user")).thenReturn(false)
        Mockito.`when`(appUserRepository.existsByEmailIgnoreCase("demo@example.com")).thenReturn(true)

        val exception = assertThrows(ResponseStatusException::class.java) {
            authService.registerUser(validRegistration())
        }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
        assertEquals("Email already exists", exception.reason)
    }

    private fun validRegistration(): RegisterUserRequest =
        RegisterUserRequest(
            displayName = "Demo User",
            username = "demo-user",
            firstName = "Demo",
            lastName = "User",
            email = "demo@example.com",
            location = "Chisinau",
            bio = "Test user",
            password = "secret123"
        )
}
