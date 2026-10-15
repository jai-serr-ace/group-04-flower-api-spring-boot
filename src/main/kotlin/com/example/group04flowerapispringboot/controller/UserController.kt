package com.example.group04flowerapispringboot.controller

import com.example.group04flowerapispringboot.model.User
import com.example.group04flowerapispringboot.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val users: UserRepository) {
    /**
     * Temporary development implementation. Replace X-User-Id with the
     * authenticated Auth0 subject once Spring Security is configured.
     */
    @GetMapping("/me")
    fun currentUser(@RequestHeader("X-User-Id") userId: Long): User =
        users.findById(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
}
