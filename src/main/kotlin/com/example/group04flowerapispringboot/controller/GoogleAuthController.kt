package com.example.group04flowerapispringboot.controller

import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class GoogleAuthRequest(val idToken: String)

@RestController
class GoogleAuthController(private val jwtDecoder: JwtDecoder) {
    @PostMapping("/api/auth/google")
    fun verify(@RequestBody request: GoogleAuthRequest): Map<String, Any?> {
        if (request.idToken.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "idToken must not be blank")
        }

        // Use the configured Google signature, issuer, expiry and audience validation.
        val jwt = try {
            jwtDecoder.decode(request.idToken)
        } catch (_: JwtException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token")
        }

        return mapOf(
            "subject" to jwt.subject,
            "issuer" to jwt.issuer.toString(),
            "audience" to jwt.audience,
            "email" to jwt.getClaimAsString("email"),
            "emailVerified" to jwt.getClaimAsBoolean("email_verified")
        )
    }
}
