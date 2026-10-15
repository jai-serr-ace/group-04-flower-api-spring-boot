package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.controller.FlowerController
import com.example.group04flowerapispringboot.model.Flower
import com.example.group04flowerapispringboot.repository.FlowerRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FlowerController::class)
class APIControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var flowers: FlowerRepository

    private val rose = Flower(
        name = "Rose",
        scientificName = "Rosa",
        locationCoord = "36.91,-121.75",
        notes = "Common garden flower"
    )

    private val roseJson = """
        {"name":"Rose","scientificName":"Rosa","locationCoord":"36.91,-121.75","notes":"Common garden flower"}
    """.trimIndent()


    @Test
    fun `getAllFlowers returns the list`() {
        given(flowers.findAll()).willReturn(listOf(rose))

        mockMvc.perform(get("/api/flowers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Rose"))
    }

    @Test
    fun `getAllFlowers returns empty list when there are none`() {
        given(flowers.findAll()).willReturn(emptyList())

        mockMvc.perform(get("/api/flowers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    // ---------- GET /api/flowers/{name} ----------

    @Test
    fun `getFlowerByName returns the flower when found`() {
        given(flowers.findByName("Rose")).willReturn(rose)

        mockMvc.perform(get("/api/flowers/Rose"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Rose"))
            .andExpect(jsonPath("$.scientificName").value("Rosa"))
    }

    @Test
    fun `getFlowerByName returns 404 when missing`() {
        given(flowers.findByName("Ghost")).willReturn(null)

        mockMvc.perform(get("/api/flowers/Ghost"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `createFlower returns 201 and saves the flower`() {
        given(flowers.exists("Rose")).willReturn(false)

        mockMvc.perform(
            post("/api/flowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roseJson)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Rose"))

        verify(flowers).insert(rose)
    }

    @Test
    fun `createFlower returns 409 when the name already exists`() {
        given(flowers.exists("Rose")).willReturn(true)

        mockMvc.perform(
            post("/api/flowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roseJson)
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `createFlower returns 400 when name is missing`() {
        mockMvc.perform(
            post("/api/flowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"scientificName":"Rosa"}""")
        )
            .andExpect(status().isBadRequest)
    }
    @Test
    fun `updateFlower returns 200 when the flower exists`() {
        given(flowers.update("Rose", rose)).willReturn(1)

        mockMvc.perform(
            put("/api/flowers/Rose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roseJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Rose"))
    }

    @Test
    fun `updateFlower returns 404 when the flower does not exist`() {
        given(flowers.update("Ghost", rose)).willReturn(0)

        mockMvc.perform(
            put("/api/flowers/Ghost")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roseJson)
        )
            .andExpect(status().isNotFound)
    }
    @Test
    fun `deleteFlower returns 204 when deleted`() {
        given(flowers.delete("Rose")).willReturn(1)

        mockMvc.perform(delete("/api/flowers/Rose"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `deleteFlower returns 404 when the flower does not exist`() {
        given(flowers.delete("Ghost")).willReturn(0)

        mockMvc.perform(delete("/api/flowers/Ghost"))
            .andExpect(status().isNotFound)
    }
}