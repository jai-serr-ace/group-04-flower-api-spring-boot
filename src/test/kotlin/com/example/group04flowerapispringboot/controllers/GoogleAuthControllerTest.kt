package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.config.SecurityConfig
import com.example.group04flowerapispringboot.controller.GoogleAuthController
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GoogleAuthController::class)
@Import(SecurityConfig::class, GoogleAuthControllerTest.TestSecurity::class)
class GoogleAuthControllerTest {
    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    class TestSecurity

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var jwtDecoder: JwtDecoder

    @MockitoBean
    lateinit var users: com.example.group04flowerapispringboot.service.CurrentUserService

    @Test
    fun `body token is validated without a bearer header or csrf token`() {
        val jwt = Jwt.withTokenValue("google-id-token")
            .header("alg", "RS256")
            .subject("google-user-id")
            .issuer("https://accounts.google.com")
            .audience(listOf("web-client-id"))
            .claim("email", "user@example.com")
            .claim("email_verified", true)
            .build()
        given(jwtDecoder.decode("google-id-token")).willReturn(jwt)
        given(users.register(jwt)).willReturn(
            com.example.group04flowerapispringboot.model.User(7, "google-user", "user@example.com", "google-user-id", false)
        )

        mockMvc.perform(post("/api/auth/google")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"idToken":"google-id-token"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subject").value("google-user-id"))
            .andExpect(jsonPath("$.issuer").value("https://accounts.google.com"))
            .andExpect(jsonPath("$.audience[0]").value("web-client-id"))
            .andExpect(jsonPath("$.email").value("user@example.com"))
            .andExpect(jsonPath("$.emailVerified").value(true))

        verify(jwtDecoder).decode("google-id-token")
    }

    @Test
    fun `decoder rejection returns unauthorized`() {
        given(jwtDecoder.decode("invalid-token"))
            .willThrow(BadJwtException("Token validation failed"))

        mockMvc.perform(post("/api/auth/google")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"idToken":"invalid-token"}"""))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `missing null blank and malformed tokens return bad request`() {
        for (body in listOf("{}", """{"idToken":null}""", """{"idToken":"   "}""", "{")) {
            mockMvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest)
        }
        verifyNoInteractions(jwtDecoder)
    }

    @Test
    fun `other routes still require authentication`() {
        mockMvc.perform(get("/api/auth-debug"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(get("/api/auth/google"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `other post routes require a bearer token`() {
        mockMvc.perform(post("/api/v1/flowers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isUnauthorized)
    }
}
