package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.config.SecurityConfig
import com.example.group04flowerapispringboot.model.User
import com.example.group04flowerapispringboot.repository.UserRepository
import com.example.group04flowerapispringboot.service.CurrentUserService
import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.web.context.WebApplicationContext

@Import(SecurityConfig::class, CurrentUserService::class, AuthenticatedControllerTest.TestSecurity::class)
abstract class AuthenticatedControllerTest {
    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    class TestSecurity

    @Autowired lateinit var context: WebApplicationContext
    @MockitoBean lateinit var users: UserRepository
    @MockitoBean lateinit var jwtDecoder: JwtDecoder
    protected lateinit var mockMvc: MockMvc
    protected val currentUser = User(7, "duck", "duck@example.com", "google-subject", false)

    @BeforeEach
    fun authenticate() {
        given(users.findByProviderId("google-subject")).willReturn(currentUser)
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .defaultRequest<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(get("/").with(jwt().jwt { it.subject("google-subject") }))
            .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(springSecurity())
            .build()
    }
}
