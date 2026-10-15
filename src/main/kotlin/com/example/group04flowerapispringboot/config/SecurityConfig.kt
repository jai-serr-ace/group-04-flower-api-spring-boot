package com.example.group04flowerapispringboot


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.http.HttpMethod

@Configuration
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers("/", "/api/v1/health", "/actuator/health").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/v1/flowers", "/api/v1/flowers/*").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt { } }
        return http.build()
    }
}