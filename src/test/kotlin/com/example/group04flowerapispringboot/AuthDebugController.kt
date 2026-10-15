package com.example.group04flowerapispringboot

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthDebugController {
    @GetMapping("/api/auth-debug")
    fun authDebug(@AuthenticationPrincipal jwt: Jwt): Map<String, Any?> =
        mapOf(
            "subject" to jwt.subject,
            "issuer" to jwt.issuer.toString(),
            "audience" to jwt.audience,
            "email" to jwt.getClaimAsString("email"),
            "emailVerified" to jwt.getClaimAsBoolean("email_verified")
        )
}