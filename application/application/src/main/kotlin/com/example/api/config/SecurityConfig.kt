package com.example.api.config

import com.example.api.repository.AppUserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/**
 * Configures password hashing, user lookup, and HTTP security rules for the application.
 */
@Configuration
class SecurityConfig {

    /**
     * Provides the password encoder used to hash stored user passwords.
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Loads application users from the database for Spring Security authentication.
     */
    @Bean
    fun userDetailsService(
        userRepository: AppUserRepository
    ): UserDetailsService = UserDetailsService { username ->
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found")

        User.withUsername(user.username)
            .password(user.passwordHash)
            .roles("USER")
            .build()
    }

    /**
     * Builds the active security filter chain based on whether authentication is enabled.
     */
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        @Value("\${app.auth.enabled:false}") authEnabled: Boolean
    ): SecurityFilterChain {
        http.csrf { it.disable() }

        if (!authEnabled) {
            http
                .authorizeHttpRequests { it.anyRequest().permitAll() }
                .formLogin { it.disable() }
                .httpBasic { it.disable() }
                .logout { it.disable() }

            return http.build()
        }

        http
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/health", "/actuator/info").permitAll()
                it.requestMatchers("/actuator/loggers", "/actuator/loggers/**").authenticated()
                it.requestMatchers(
                    "/login",
                    "/login.html",
                    "/register",
                    "/register.html",
                    "/error",
                    "/favicon.ico",
                    "/api/v1/auth/register",
                    "/api/v1/auth/status"
                ).permitAll()
                it.anyRequest().authenticated()
            }
            .formLogin {
                it.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error")
                    .permitAll()
            }
            .logout {
                it.logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
            }
            .httpBasic { it.disable() }

        return http.build()
    }
}
