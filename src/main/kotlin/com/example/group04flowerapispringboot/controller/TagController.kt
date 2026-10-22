package com.example.group04flowerapispringboot.controller

import com.example.group04flowerapispringboot.model.Tag
import com.example.group04flowerapispringboot.repository.TagRepository
import com.example.group04flowerapispringboot.repository.FlowerRepository
import com.example.group04flowerapispringboot.service.CurrentUserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class TagRequest(val userTag: String)

@RestController
@RequestMapping("/api/v1/tags")
class TagController(private val tags: TagRepository, private val flowers: FlowerRepository, private val users: CurrentUserService) {
    @PostMapping("/{flowerId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun addTag(
        @PathVariable flowerId: Long,
        @RequestBody request: TagRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): Tag {
        requireOwner(flowerId, jwt)
        return tags.create(flowerId, request.userTag)
    }

    // The documented path /{flower_id,user_tag} is not valid Spring syntax.
    // Use /api/v1/tags/{flowerId}/{userTag} instead.
    @DeleteMapping("/{flowerId}/{userTag}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTag(
        @PathVariable flowerId: Long,
        @PathVariable userTag: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        requireOwner(flowerId, jwt)
        if (!tags.delete(flowerId, userTag)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found")
        }
    }

    private fun requireOwner(flowerId: Long, jwt: Jwt) {
        val userId = users.current(jwt).userId
        if (flowers.findById(flowerId)?.userId != userId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flower not found or not owned by you")
        }
    }
}
