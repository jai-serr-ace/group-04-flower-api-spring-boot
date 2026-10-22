package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.controller.UserController
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
class UserControllerTest : AuthenticatedControllerTest() {
    @Test
    fun `me uses the verified subject and ignores spoofed user header`() {
        mockMvc.perform(get("/api/v1/users/me").header("X-User-Id", "99"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(7))
        verify(users).findByProviderId("google-subject")
    }

    @Test
    fun `me returns 404 until the Google user signs in`() {
        given(users.findByProviderId("unregistered-subject")).willReturn(null)
        mockMvc.perform(get("/api/v1/users/me").with(jwt().jwt { it.subject("unregistered-subject") }))
            .andExpect(status().isNotFound)
    }
}
