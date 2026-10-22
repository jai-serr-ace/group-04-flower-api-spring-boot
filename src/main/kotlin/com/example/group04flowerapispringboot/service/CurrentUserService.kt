package com.example.group04flowerapispringboot.service

import com.example.group04flowerapispringboot.model.User
import com.example.group04flowerapispringboot.repository.UserRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CurrentUserService(private val users: UserRepository) {
    fun current(jwt: Jwt): User = users.findByProviderId(subject(jwt))
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Sign in with /api/auth/google first")

    fun register(jwt: Jwt): User {
        val subject = subject(jwt)
        users.findByProviderId(subject)?.let { return it }
        val email = jwt.getClaimAsString("email")
        if (jwt.getClaimAsBoolean("email_verified") != true || email.isNullOrBlank() || email.length > 255) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "A verified Google email is required")
        }
        try {
            return users.registerGoogleUser(subject, email)
        } catch (_: DuplicateKeyException) {
            // Never link an existing account merely because its email matches.
            throw ResponseStatusException(HttpStatus.CONFLICT, "Account already exists; account linking is required")
        }
    }

    private fun subject(jwt: Jwt): String = jwt.subject
        ?.takeIf { it.isNotBlank() && it.length <= 248 }
        ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google subject")
}
