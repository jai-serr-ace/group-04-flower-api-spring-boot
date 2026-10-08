package com.example.group04flowerapispringboot.controller

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
    private val jdbcTemplate: JdbcTemplate
) {
    @GetMapping("/api/v1/health")
    fun health(): Map<String, String> {
        jdbcTemplate.queryForObject("SELECT 1", Int::class.java)

        return mapOf(
            "status" to "ok",
            "database" to "connected"
        )
    }
}