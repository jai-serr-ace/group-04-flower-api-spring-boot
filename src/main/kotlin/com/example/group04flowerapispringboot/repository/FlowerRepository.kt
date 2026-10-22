package com.example.group04flowerapispringboot.repository

import com.example.group04flowerapispringboot.model.Flower
import com.example.group04flowerapispringboot.model.PageResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import kotlin.math.ceil

@Repository
class FlowerRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val flowerMapper = RowMapper<Flower> { resultSet, _ ->
        Flower(
            flowerId = resultSet.getLong("flower_id"),
            scientificName = resultSet.getString("scientific_name"),
            userId = resultSet.getLong("user_id"),
            name = resultSet.getString("name"),
            locationCoord = resultSet.getString("location_coord"),
            notes = resultSet.getString("notes")
        )
    }

    fun findPage(
        page: Int,
        size: Int,
        name: String?,
        scientificName: String?,
        sort: String,
        direction: String
    ): PageResponse<Flower> {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 100)
        val filters = mutableListOf<String>()
        val filterParameters = mutableListOf<Any>()

        if (!name.isNullOrBlank()) {
            filters += "LOWER(name) LIKE LOWER(?)"
            filterParameters += "%${name.trim()}%"
        }

        if (!scientificName.isNullOrBlank()) {
            filters += "LOWER(scientific_name) LIKE LOWER(?)"
            filterParameters += "%${scientificName.trim()}%"
        }

        val whereClause = if (filters.isEmpty()) {
            ""
        } else {
            "WHERE ${filters.joinToString(" AND ")}"
        }

        // Only allow known column names. Never insert the raw query parameter
        // into SQL, because that would allow SQL injection through `sort`.
        val sortColumn = when (sort) {
            "name" -> "name"
            "scientificName" -> "scientific_name"
            "userId" -> "user_id"
            "flowerId" -> "flower_id"
            else -> "flower_id"
        }

        val sortDirection = if (direction.equals("asc", ignoreCase = true)) {
            "ASC"
        } else {
            "DESC"
        }

        val totalItems = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flowers $whereClause",
            Long::class.javaObjectType,
            *filterParameters.toTypedArray()
        ) ?: 0L

        val offset = safePage * safeSize
        val queryParameters = filterParameters.toMutableList()
        queryParameters += safeSize
        queryParameters += offset

        val items = jdbcTemplate.query(
            """
            SELECT flower_id,
                   scientific_name,
                   user_id,
                   name,
                   location_coord,
                   notes
            FROM flowers
            $whereClause
            ORDER BY $sortColumn $sortDirection
            LIMIT ? OFFSET ?
            """.trimIndent(),
            flowerMapper,
            *queryParameters.toTypedArray()
        )

        val totalPages = if (totalItems == 0L) {
            0
        } else {
            ceil(totalItems.toDouble() / safeSize).toInt()
        }

        return PageResponse(
            items = items,
            page = safePage,
            size = safeSize,
            totalItems = totalItems,
            totalPages = totalPages
        )
    }

    fun findById(flowerId: Long): Flower? {
        return jdbcTemplate.query(
            """
            SELECT flower_id,
                   scientific_name,
                   user_id,
                   name,
                   location_coord,
                   notes
            FROM flowers
            WHERE flower_id = ?
            """.trimIndent(),
            flowerMapper,
            flowerId
        ).firstOrNull()
    }

    fun create(flower: Flower): Flower {
        val flowerId = jdbcTemplate.queryForObject(
            """
            INSERT INTO flowers (
                scientific_name,
                user_id,
                name,
                location_coord,
                notes
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING flower_id
            """.trimIndent(),
            Long::class.javaObjectType,
            flower.scientificName,
            flower.userId,
            flower.name,
            flower.locationCoord,
            flower.notes
        ) ?: error("The database did not return a flower ID")

        return findById(flowerId)
            ?: error("The created flower could not be found")
    }

    fun update(flowerId: Long, flower: Flower): Flower? {
        val changed = jdbcTemplate.update(
            """
            UPDATE flowers
            SET scientific_name = ?,
                name = ?,
                location_coord = ?,
                notes = ?
            WHERE flower_id = ? AND user_id = ?
            """.trimIndent(),
            flower.scientificName,
            flower.name,
            flower.locationCoord,
            flower.notes,
            flowerId,
            flower.userId
        )

        return if (changed > 0) findById(flowerId) else null
    }

    fun delete(flowerId: Long, userId: Long): Boolean {
        return jdbcTemplate.update(
            "DELETE FROM flowers WHERE flower_id = ? AND user_id = ?",
            flowerId,
            userId
        ) > 0
    }
}
