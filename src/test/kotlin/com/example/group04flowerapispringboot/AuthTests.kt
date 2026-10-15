package com.example.group04flowerapispringboot

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
class AuthTests(@Autowired val mockMvc: MockMvc) {
    @Test
    fun `request without token is rejected`() {
        mockMvc.perform(get("/api/flowers"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `request with mocked Google token is accepted`() {
        mockMvc.perform(
            get("/api/flowers")
                .with(
                    jwt().jwt { token ->
                        token
                            .subject("test-user-id")
                            .issuer("https://accounts.google.com")
                            .audience(
                                listOf(
                                    "787686861255-b1fasim7iu03jgs32ncdde5hs7ld31c9.apps.googleusercontent.com"
                                )
                            )
                    }
                )
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `auth debug endpoint receives and decodes token claims`() {
        mockMvc.perform(
            get("/api/auth-debug")
                .with(
                    jwt().jwt { token ->
                        token
                            .subject("test-user-id")
                            .issuer("https://accounts.google.com")
                            .audience(listOf("test-client-id"))
                            .claim("email", "test@example.com")
                            .claim("email_verified", true)
                    }
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subject").value("test-user-id"))
            .andExpect(jsonPath("$.issuer").value("https://accounts.google.com"))
            .andExpect(jsonPath("$.audience[0]").value("test-client-id"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.emailVerified").value(true))
    }
}