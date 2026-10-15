package com.example.group04flowerapispringboot.controller

import com.example.group04flowerapispringboot.model.Flower
import com.example.group04flowerapispringboot.repository.FlowerRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api")
class FlowerController(private val flowers: FlowerRepository) {
    @GetMapping("/flowers")
    fun getAllFlowers(): List<Flower> = flowers.findAll()

    @GetMapping("/flowers/{name}")
    fun getFlowerByName(@PathVariable name: String): Flower =
        flowers.findByName(name)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No flower named '$name'")

    @PostMapping("/flowers")
    @ResponseStatus(HttpStatus.CREATED)
    fun createFlower(@RequestBody flower: Flower): Flower {
        if (flowers.exists(flower.name))
            throw ResponseStatusException(HttpStatus.CONFLICT, "'${flower.name}' already exists")
        flowers.insert(flower)
        return flower
    }

    @PutMapping("/flowers/{name}")
    fun updateFlower(@PathVariable name: String, @RequestBody flower: Flower): Flower {
        if (flowers.update(name, flower) == 0)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No flower named '$name'")
        return flower
    }

    @DeleteMapping("/flowers/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteFlower(@PathVariable name: String) {
        if (flowers.delete(name) == 0)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No flower named '$name'")
    }
}