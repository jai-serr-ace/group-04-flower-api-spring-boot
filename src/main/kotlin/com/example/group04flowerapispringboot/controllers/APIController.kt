package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.models.Default
import com.example.group04flowerapispringboot.models.Flower
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

@RestController
@RequestMapping("/api")
class APIController {
    /*@GetMapping("/greet")
    fun getDefault(
        @RequestParam("name")
        name: String
    ): Default {
        return Default("Hello, $name!")
    }*/
    @GetMapping("/flowers")
    fun getAllFlowers(): List<Flower> {
        // TODO: get all flowers from the database
        return listOf(Flower(name = "placeholder"))
    }

    @GetMapping("/flowers/{name}")
    fun getFlowerByName(@PathVariable name: String): Flower {
        // TODO: look up the flower by name in the database
        return Flower(name = name)
    }

    @PostMapping("/flowers")
    @ResponseStatus(HttpStatus.CREATED)
    fun createFlower(@RequestBody flower: Flower): Flower {
        // TODO: save the flower to the database
        return flower
    }

    @PutMapping("/flowers/{name}")
    fun updateFlower(@PathVariable name: String, @RequestBody flower: Flower): Flower {
        // TODO: update the flower in the database
        return flower
    }

    @DeleteMapping("/flowers/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteFlower(@PathVariable name: String) {
        // TODO: delete the flower from the database
    }

}