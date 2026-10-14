package com.example.group04flowerapispringboot.repository

import com.example.group04flowerapispringboot.model.Tag
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TagRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(flowerId: Long, userTag: String): Tag {
        jdbcTemplate.update(
            "INSERT INTO tags (flower_id, user_tag) VALUES (?, ?)",
            flowerId,
            userTag
        )
        return Tag(flowerId, userTag)
    }

    fun delete(flowerId: Long, userTag: String): Boolean = jdbcTemplate.update(
        "DELETE FROM tags WHERE flower_id = ? AND user_tag = ?",
        flowerId,
        userTag
    ) > 0
}
