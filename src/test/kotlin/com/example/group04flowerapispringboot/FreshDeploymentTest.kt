package com.example.group04flowerapispringboot

import com.example.group04flowerapispringboot.service.CurrentUserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

// Opt in with FLOWER_POSTGRES_TEST=true against an EMPTY, disposable PostgreSQL
// database with TLS. This suite runs the real deployment Flyway migration.
@EnabledIfEnvironmentVariable(named = "FLOWER_POSTGRES_TEST", matches = "true")
@ActiveProfiles("deploy")
@SpringBootTest(properties = [
    "PGHOST=\${FLOWER_TEST_DB_HOST:127.0.0.1}", "PGPORT=\${FLOWER_TEST_DB_PORT:5434}",
    "PGDATABASE=\${FLOWER_TEST_DB_NAME:flower_deploy_test}",
    "PGUSER=\${FLOWER_TEST_DB_USER:floweruser}", "PGPASSWORD=\${FLOWER_TEST_DB_PASSWORD:test}",
    "GOOGLE_WEB_CLIENT_ID=test-client-id", "DEPLOY_MIGRATIONS_ENABLED=true",
    "spring.flyway.enabled=true",
    "spring.datasource.url=jdbc:postgresql://\${PGHOST}:\${PGPORT}/\${PGDATABASE}?sslmode=require",
    "spring.datasource.username=\${PGUSER}", "spring.datasource.password=\${PGPASSWORD}"
])
@AutoConfigureMockMvc
@Transactional
class FreshDeploymentTest(
    @Autowired private val mvc: MockMvc,
    @Autowired private val users: CurrentUserService,
    @Autowired private val jdbc: JdbcTemplate
) {
    private fun token(subject: String, email: String = "$subject@example.com", verified: Boolean = true) =
        Jwt.withTokenValue("test").header("alg", "RS256").subject(subject)
            .issuer("https://accounts.google.com").audience(listOf("test-client-id"))
            .claim("email", email).claim("email_verified", verified).build()

    @Test
    fun `new database supports registration retrieval and owner-only writes over TLS`() {
        assertTrue(jdbc.queryForObject("SELECT ssl FROM pg_stat_ssl WHERE pid = pg_backend_pid()", Boolean::class.java)!!)
        mvc.perform(get("/api/v1/health")).andExpect(status().isOk)
            .andExpect(jsonPath("$.database").value("connected"))
        val ownerToken = token("owner")
        val otherToken = token("other")
        val owner = users.register(ownerToken)
        users.register(otherToken)
        assertEquals(owner.userId, users.register(ownerToken).userId)

        mvc.perform(get("/api/v1/users/me").with(jwt().jwt(ownerToken)).header("X-User-Id", "999"))
            .andExpect(status().isOk).andExpect(jsonPath("$.userId").value(owner.userId))
        mvc.perform(post("/api/v1/flowers").with(jwt().jwt(ownerToken))
            .contentType(MediaType.APPLICATION_JSON).content("""{"name":"Rose","userId":999}"""))
            .andExpect(status().isForbidden)
        mvc.perform(post("/api/v1/flowers").with(jwt().jwt(ownerToken))
            .contentType(MediaType.APPLICATION_JSON).content("""{"name":"Rose"}"""))
            .andExpect(status().isCreated).andExpect(jsonPath("$.userId").value(owner.userId))
        val id = jdbc.queryForObject("SELECT flower_id FROM flowers WHERE user_id = ?", Long::class.java, owner.userId)!!
        mvc.perform(get("/api/v1/flowers/$id")).andExpect(status().isOk)
        mvc.perform(patch("/api/v1/flowers/$id").with(jwt().jwt(otherToken))
            .contentType(MediaType.APPLICATION_JSON).content("""{"name":"Stolen"}"""))
            .andExpect(status().isNotFound)
        mvc.perform(delete("/api/v1/flowers/$id").with(jwt().jwt(otherToken)))
            .andExpect(status().isNotFound)
        mvc.perform(post("/api/v1/tags/$id").with(jwt().jwt(otherToken))
            .contentType(MediaType.APPLICATION_JSON).content("""{"userTag":"red"}"""))
            .andExpect(status().isNotFound)
        mvc.perform(patch("/api/v1/flowers/$id").with(jwt().jwt(ownerToken))
            .contentType(MediaType.APPLICATION_JSON).content("""{"name":"Updated"}"""))
            .andExpect(status().isOk).andExpect(jsonPath("$.name").value("Updated"))
        mvc.perform(post("/api/v1/tags/$id").with(jwt().jwt(ownerToken))
            .contentType(MediaType.APPLICATION_JSON).content("""{"userTag":"red"}"""))
            .andExpect(status().isCreated)
        mvc.perform(delete("/api/v1/tags/$id/red").with(jwt().jwt(otherToken)))
            .andExpect(status().isNotFound)
        mvc.perform(delete("/api/v1/tags/$id/red").with(jwt().jwt(ownerToken)))
            .andExpect(status().isNoContent)
        mvc.perform(delete("/api/v1/flowers/$id").with(jwt().jwt(ownerToken)))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `registration rejects an unverified email without creating an account`() {
        val error = assertFailsWith<ResponseStatusException> { users.register(token("unverified", verified = false)) }
        assertEquals(401, error.statusCode.value())
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM users", Int::class.java))
    }

    @Test
    fun `matching email never links a different Google identity`() {
        users.register(token("first", "shared@example.com"))
        val error = assertFailsWith<ResponseStatusException> { users.register(token("second", "shared@example.com")) }
        assertEquals(409, error.statusCode.value())
    }
}
