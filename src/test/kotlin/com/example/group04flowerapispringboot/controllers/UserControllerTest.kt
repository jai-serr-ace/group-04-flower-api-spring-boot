package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.controller.UserController
import com.example.group04flowerapispringboot.model.User
import com.example.group04flowerapispringboot.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var users: UserRepository

    private val base = "/api/v1/users"

    private val oscar = User(
        userId = 7,
        username = "duck",
        email = "duck@gmail.com",
        providerId = "duck",
        isAdmin = false
    )

    @Test
    fun `currentUser returns the user when found`() {
        given(users.findById(7L)).willReturn(oscar)

        mockMvc.perform(get("$base/me").header("X-User-Id", "7"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(7))
            .andExpect(jsonPath("$.username").value("duck"))
            .andExpect(jsonPath("$.email").value("duck@gmail.com"))
            .andExpect(jsonPath("$.providerId").value("duck"))
    }

    @Test
    fun `currentUser returns 404 when the user does not exist`() {
        given(users.findById(99L)).willReturn(null)

        mockMvc.perform(get("$base/me").header("X-User-Id", "99"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `currentUser returns 400 when the X-User-Id header is missing`() {
        mockMvc.perform(get("$base/me"))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(users)
    }

    @Test
    fun `currentUser returns 400 when the X-User-Id header is not a number`() {
        mockMvc.perform(get("$base/me").header("X-User-Id", "abc"))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(users)
    }
}