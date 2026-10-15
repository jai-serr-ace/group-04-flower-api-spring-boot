package com.example.group04flowerapispringboot.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
/**
 * Controlls root directory
 *
 * @author: Braeden Migay
 * @since: 10/14/26
 * @version 0.1.0
 */

@RestController
class RootController {
    @GetMapping("/")
    fun root(): Map<String, String> =
        mapOf("service" to "flower-api", "status" to "ok")
}