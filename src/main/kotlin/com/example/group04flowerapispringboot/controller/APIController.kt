package com.example.group04flowerapispringboot.controller

import com.example.group04flowerapispringboot.model.Default
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class APIController {
    @GetMapping("/greet")
    fun getDefault(
        @RequestParam("name")
        name: String
    ): Default {
        return Default("Hello, $name!")
    }
}