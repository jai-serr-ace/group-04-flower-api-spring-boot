package com.example.group04flowerapispringboot.controller

import com.example.group04flowerapispringboot.model.Flower
import com.example.group04flowerapispringboot.model.PageResponse
import com.example.group04flowerapispringboot.repository.FlowerRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class FlowerRequest(
    val scientificName: String? = null,
    val userId: Long,
    val name: String,
    val locationCoord: String? = null,
    val notes: String? = null
)

@RestController
@RequestMapping("/api/v1/flowers")
class FlowerController(private val flowers: FlowerRepository) {
    @GetMapping
    fun listFlowers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) scientificName: String?,
        @RequestParam(defaultValue = "flowerId") sort: String,
        @RequestParam(defaultValue = "desc") direction: String
    ): PageResponse<Flower> {
        if (page < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be zero or greater")
        }

        if (size !in 1..100) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100")
        }

        if (direction.lowercase() !in setOf("asc", "desc")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "direction must be asc or desc")
        }

        return flowers.findPage(
            page = page,
            size = size,
            name = name,
            scientificName = scientificName,
            sort = sort,
            direction = direction
        )
    }

    @GetMapping("/{flowerId}")
    fun getFlower(@PathVariable flowerId: Long): Flower =
        flowers.findById(flowerId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flower not found")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createFlower(@RequestBody request: FlowerRequest): Flower = flowers.create(
        Flower(
            flowerId = 0,
            scientificName = request.scientificName,
            userId = request.userId,
            name = request.name,
            locationCoord = request.locationCoord,
            notes = request.notes
        )
    )

    @PatchMapping("/{flowerId}")
    fun updateFlower(
        @PathVariable flowerId: Long,
        @RequestBody request: FlowerRequest
    ): Flower = flowers.update(
        flowerId,
        Flower(
            flowerId = flowerId,
            scientificName = request.scientificName,
            userId = request.userId,
            name = request.name,
            locationCoord = request.locationCoord,
            notes = request.notes
        )
    ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flower not found")

    @DeleteMapping("/{flowerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteFlower(@PathVariable flowerId: Long) {
        if (!flowers.delete(flowerId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flower not found")
        }
    }
}
