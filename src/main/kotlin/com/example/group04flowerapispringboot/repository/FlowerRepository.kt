package com.example.group04flowerapispringboot.repository

import com.example.group04flowerapispringboot.model.Flower
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

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

    fun findAll(): List<Flower> {
        return jdbcTemplate.query(
            """
            SELECT flower_id,
                   scientific_name,
                   user_id,
                   name,
                   location_coord,
                   notes
            FROM flowers
            ORDER BY flower_id DESC
            """.trimIndent(),
            flowerMapper
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
        jdbcTemplate.update(
            """
            UPDATE flowers
            SET scientific_name = ?,
                name = ?,
                location_coord = ?,
                notes = ?
            WHERE flower_id = ?
            """.trimIndent(),
            flower.scientificName,
            flower.name,
            flower.locationCoord,
            flower.notes,
            flowerId
        )

        return findById(flowerId)
    }

    fun delete(flowerId: Long): Boolean {
        return jdbcTemplate.update(
            "DELETE FROM flowers WHERE flower_id = ?",
            flowerId
        ) > 0
    }
}