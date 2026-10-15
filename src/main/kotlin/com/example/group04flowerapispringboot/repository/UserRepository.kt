package com.example.group04flowerapispringboot.repository

import com.example.group04flowerapispringboot.model.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class UserRepository(private val jdbcTemplate: JdbcTemplate) {
    private val mapper = RowMapper<User> { rs, _ ->
        User(
            userId = rs.getLong("user_id"),
            username = rs.getString("username"),
            email = rs.getString("email"),
            providerId = rs.getString("provider_id"),
            isAdmin = rs.getBoolean("is_admin")
        )
    }

    fun findById(userId: Long): User? = jdbcTemplate.query(
        """
        SELECT user_id, username, email, provider_id, is_admin
        FROM users
        WHERE user_id = ?
        """.trimIndent(),
        mapper,
        userId
    ).firstOrNull()
}
