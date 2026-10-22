package com.example.group04flowerapispringboot.config


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.http.HttpMethod
import org.springframework.security.config.http.SessionCreationPolicy
import jakarta.servlet.DispatcherType

@Configuration
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Clients explicitly send bearer tokens; authentication never uses cookies.
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                // Preserve controller error statuses through the container's error dispatch.
                it.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                it.requestMatchers(HttpMethod.POST, "/api/auth/google").permitAll()
                it.requestMatchers("/", "/api/v1/health", "/actuator/health").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/v1/flowers", "/api/v1/flowers/*").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt { } }
        return http.build()
    }
}
