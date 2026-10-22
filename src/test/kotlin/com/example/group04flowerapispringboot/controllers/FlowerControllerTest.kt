package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.controller.FlowerController
import com.example.group04flowerapispringboot.model.Flower
import com.example.group04flowerapispringboot.model.PageResponse
import com.example.group04flowerapispringboot.repository.FlowerRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FlowerController::class)
class FlowerControllerTest : AuthenticatedControllerTest() {


    @MockitoBean
    lateinit var flowers: FlowerRepository

    private val base = "/api/v1/flowers"

    private val rose = Flower(
        flowerId = 1,
        scientificName = "Rosa",
        userId = 7,
        name = "Rose",
        locationCoord = "36.91,-121.75",
        notes = "Common garden flower"
    )

    private val roseJson = """
        {"name":"Rose","scientificName":"Rosa","userId":7,"locationCoord":"36.91,-121.75","notes":"Common garden flower"}
    """.trimIndent()


    @Test
    fun `listFlowers returns a page using the default parameters`() {
        given(flowers.findPage(0, 20, null, null, "flowerId", "desc"))
            .willReturn(
                PageResponse(
                    items = listOf(rose),
                    page = 0,
                    size = 20,
                    totalItems = 1,
                    totalPages = 1
                )
            )

        mockMvc.perform(get(base))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].name").value("Rose"))
            .andExpect(jsonPath("$.items[0].flowerId").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalItems").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    fun `listFlowers passes filters sort and paging through to the repository`() {
        given(flowers.findPage(2, 5, "ros", "ros", "name", "asc"))
            .willReturn(
                PageResponse(
                    items = emptyList(),
                    page = 2,
                    size = 5,
                    totalItems = 0,
                    totalPages = 0
                )
            )

        mockMvc.perform(
            get(base)
                .param("page", "2")
                .param("size", "5")
                .param("name", "ros")
                .param("scientificName", "ros")
                .param("sort", "name")
                .param("direction", "asc")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(0))
            .andExpect(jsonPath("$.page").value(2))

        verify(flowers).findPage(2, 5, "ros", "ros", "name", "asc")
    }

    @Test
    fun `listFlowers returns 400 when page is negative`() {
        mockMvc.perform(get(base).param("page", "-1"))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(flowers)
    }

    @Test
    fun `listFlowers returns 400 when size is 0`() {
        mockMvc.perform(get(base).param("size", "0"))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(flowers)
    }

    @Test
    fun `listFlowers returns 400 when size is over 100`() {
        mockMvc.perform(get(base).param("size", "101"))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(flowers)
    }

    @Test
    fun `listFlowers returns 400 when direction is not asc or desc`() {
        mockMvc.perform(get(base).param("direction", "sideways"))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(flowers)
    }


    @Test
    fun `getFlower returns the flower when found`() {
        given(flowers.findById(1L)).willReturn(rose)

        mockMvc.perform(get("$base/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.flowerId").value(1))
            .andExpect(jsonPath("$.name").value("Rose"))
            .andExpect(jsonPath("$.scientificName").value("Rosa"))
            .andExpect(jsonPath("$.userId").value(7))
    }

    @Test
    fun `getFlower returns 404 when missing`() {
        given(flowers.findById(99L)).willReturn(null)

        mockMvc.perform(get("$base/99"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getFlower returns 400 when the id is not a number`() {
        mockMvc.perform(get("$base/abc"))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(flowers)
    }


    @Test
    fun `createFlower returns 201 and the saved flower`() {
        val toSave = rose.copy(flowerId = 0)
        given(flowers.create(toSave)).willReturn(rose)

        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(roseJson)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.flowerId").value(1))
            .andExpect(jsonPath("$.name").value("Rose"))

        verify(flowers).create(toSave)
    }

    @Test
    fun `createFlower returns 400 when name is missing`() {
        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"scientificName":"Rosa","userId":7}""")
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(flowers)
    }

    @Test
    fun `createFlower derives owner when userId is missing`() {
        val saved = rose.copy(scientificName = null, locationCoord = null, notes = null)
        given(flowers.create(saved.copy(flowerId = 0))).willReturn(saved)
        mockMvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content("""{"name":"Rose"}"""))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.userId").value(7))
    }

    @Test
    fun `createFlower rejects another user as owner`() {
        mockMvc.perform(post(base).contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Rose","userId":99}"""))
            .andExpect(status().isForbidden)
        verifyNoInteractions(flowers)
    }


    @Test
    fun `updateFlower returns 200 and the updated flower`() {
        val updated = rose.copy(notes = "Updated notes")
        given(flowers.update(1L, rose)).willReturn(updated)

        mockMvc.perform(
            patch("$base/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roseJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.flowerId").value(1))
            .andExpect(jsonPath("$.notes").value("Updated notes"))
    }

    @Test
    fun `updateFlower returns 404 when the flower does not exist`() {
        given(flowers.update(99L, rose.copy(flowerId = 99))).willReturn(null)

        mockMvc.perform(
            patch("$base/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roseJson)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `updateFlower returns 400 when the body is missing required fields`() {
        mockMvc.perform(
            patch("$base/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"notes":"only notes"}""")
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(flowers)
    }
    @Test
    fun `deleteFlower returns 204 when deleted`() {
        given(flowers.delete(1L, 7L)).willReturn(true)

        mockMvc.perform(delete("$base/1"))
            .andExpect(status().isNoContent)

        verify(flowers).delete(1L, 7L)
    }

    @Test
    fun `deleteFlower returns 404 when the flower does not exist`() {
        given(flowers.delete(99L, 7L)).willReturn(false)

        mockMvc.perform(delete("$base/99"))
            .andExpect(status().isNotFound)
    }
}