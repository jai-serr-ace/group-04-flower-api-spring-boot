package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.controller.TagController
import com.example.group04flowerapispringboot.model.Tag
import com.example.group04flowerapispringboot.repository.TagRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(TagController::class)
class TagControllerTest : AuthenticatedControllerTest() {


    @MockitoBean
    lateinit var tags: TagRepository

    @MockitoBean lateinit var flowers: com.example.group04flowerapispringboot.repository.FlowerRepository

    @org.junit.jupiter.api.BeforeEach
    fun ownFlower() {
        given(flowers.findById(1L)).willReturn(
            com.example.group04flowerapispringboot.model.Flower(1, null, 7, "Rose", null, null)
        )
    }

    private val base = "/api/v1/tags"

    private val redTag = Tag(flowerId = 1, userTag = "red")
    @Test
    fun `addTag returns 201 and the saved tag`() {
        given(tags.create(1L, "red")).willReturn(redTag)

        mockMvc.perform(
            post("$base/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userTag":"red"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.flowerId").value(1))
            .andExpect(jsonPath("$.userTag").value("red"))

        verify(tags).create(1L, "red")
    }

    @Test
    fun `addTag returns 400 when userTag is missing`() {
        mockMvc.perform(
            post("$base/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(tags)
    }

    @Test
    fun `addTag returns 400 when the flower id is not a number`() {
        mockMvc.perform(
            post("$base/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userTag":"red"}""")
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(tags)
    }

    @Test
    fun `deleteTag returns 204 when deleted`() {
        given(tags.delete(1L, "red")).willReturn(true)

        mockMvc.perform(delete("$base/1/red"))
            .andExpect(status().isNoContent)

        verify(tags).delete(1L, "red")
    }

    @Test
    fun `deleteTag returns 404 when the tag does not exist`() {
        given(tags.delete(1L, "ghost")).willReturn(false)

        mockMvc.perform(delete("$base/1/ghost"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `cannot tag another users flower`() {
        given(flowers.findById(1L)).willReturn(
            com.example.group04flowerapispringboot.model.Flower(1, null, 99, "Rose", null, null)
        )
        mockMvc.perform(post("$base/1").contentType(MediaType.APPLICATION_JSON)
            .content("""{"userTag":"red"}"""))
            .andExpect(status().isNotFound)
        verifyNoInteractions(tags)
    }
}
