package com.example.group04flowerapispringboot.controller

import com.example.group04flowerapispringboot.model.User
import com.example.group04flowerapispringboot.service.CurrentUserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val users: CurrentUserService) {
    @GetMapping("/me")
    fun currentUser(@AuthenticationPrincipal jwt: Jwt): User = users.current(jwt)
}
