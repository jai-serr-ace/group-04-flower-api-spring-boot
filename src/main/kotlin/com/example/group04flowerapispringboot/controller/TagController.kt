package com.example.group04flowerapispringboot.controller

import com.example.group04flowerapispringboot.model.Tag
import com.example.group04flowerapispringboot.repository.TagRepository
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
class TagController(private val tags: TagRepository) {
    @PostMapping("/{flowerId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun addTag(
        @PathVariable flowerId: Long,
        @RequestBody request: TagRequest
    ): Tag = tags.create(flowerId, request.userTag)

    // The documented path /{flower_id,user_tag} is not valid Spring syntax.
    // Use /api/v1/tags/{flowerId}/{userTag} instead.
    @DeleteMapping("/{flowerId}/{userTag}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTag(
        @PathVariable flowerId: Long,
        @PathVariable userTag: String
    ) {
        if (!tags.delete(flowerId, userTag)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found")
        }
    }
}
