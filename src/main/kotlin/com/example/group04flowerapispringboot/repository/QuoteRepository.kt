package com.example.group04flowerapispringboot.repository

import com.example.group04flowerapispringboot.model.Quote
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class QuoteRepository(private val jdbcTemplate: JdbcTemplate) {
    private val mapper = RowMapper<Quote> { rs, _ ->
        Quote(
            quoteId = rs.getLong("quote_id"),
            quoteText = rs.getString("quote_text"),
            author = rs.getString("author")
        )
    }

    fun findAll(): List<Quote> = jdbcTemplate.query(
        "SELECT quote_id, quote_text, author FROM quotes ORDER BY quote_id DESC",
        mapper
    )

    fun findById(quoteId: Long): Quote? = jdbcTemplate.query(
        "SELECT quote_id, quote_text, author FROM quotes WHERE quote_id = ?",
        mapper,
        quoteId
    ).firstOrNull()

    fun create(quoteText: String, author: String?): Quote {
        val id = jdbcTemplate.queryForObject(
            """
            INSERT INTO quotes (quote_text, author)
            VALUES (?, ?)
            RETURNING quote_id
            """.trimIndent(),
            Long::class.java,
            quoteText,
            author
        ) ?: error("The database did not return a quote ID")

        return findById(id) ?: error("The created quote could not be found")
    }
}
