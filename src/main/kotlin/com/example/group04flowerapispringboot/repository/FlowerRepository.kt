package com.example.group04flowerapispringboot.repository

import com.example.group04flowerapispringboot.model.Flower
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class FlowerRepository(private val jdbc: JdbcTemplate) {

    private val mapper = RowMapper<Flower> { rs, _ ->
        Flower(
            name = rs.getString("name"),
            scientificName = rs.getString("scientific_name"),
            locationCoord = rs.getString("location_coord"),
            notes = rs.getString("notes")
        )
    }

    private val cols = "name, scientific_name, location_coord, notes"

    // flowers.user_id is NOT NULL, so every flower needs a user.
    // Until there is login, everything belongs to one placeholder user.
    private fun placeholderUserId(): Long {
        jdbc.update(
            "INSERT INTO users (username, email) VALUES ('default', 'default@example.com') " +
                    "ON CONFLICT DO NOTHING"
        )
        return jdbc.queryForObject(
            "SELECT user_id FROM users WHERE username = 'default'",
            Long::class.javaObjectType
        )!!
    }

    fun findAll(): List<Flower> =
        jdbc.query("SELECT $cols FROM flowers ORDER BY name", mapper)

    fun findByName(name: String): Flower? =
        jdbc.query("SELECT $cols FROM flowers WHERE LOWER(name) = LOWER(?)", mapper, name)
            .firstOrNull()

    fun exists(name: String): Boolean =
        findByName(name) != null

    fun insert(f: Flower) {
        jdbc.update(
            "INSERT INTO flowers (user_id, name, scientific_name, location_coord, notes) " +
                    "VALUES (?, ?, ?, ?, ?)",
            placeholderUserId(), f.name, f.scientificName, f.locationCoord, f.notes
        )
    }
    fun update(name: String, f: Flower): Int =
        jdbc.update(
            "UPDATE flowers SET name = ?, scientific_name = ?, location_coord = ?, notes = ? " +
                    "WHERE LOWER(name) = LOWER(?)",
            f.name, f.scientificName, f.locationCoord, f.notes, name
        )

    fun delete(name: String): Int =
        jdbc.update("DELETE FROM flowers WHERE LOWER(name) = LOWER(?)", name)
}