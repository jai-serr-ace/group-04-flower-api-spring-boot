package com.example.group04flowerapispringboot

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals

@ActiveProfiles("deploy")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "PGHOST=localhost", "PGPORT=5432", "PGDATABASE=test", "PGUSER=test",
        "PGPASSWORD=test", "GOOGLE_WEB_CLIENT_ID=test-client-id",
        "spring.flyway.enabled=false"
    ]
)
class DeploymentHttpTest(@Autowired private val environment: Environment) {
    private val client = HttpClient.newHttpClient()

    private fun request(path: String, body: String? = null): Int {
        val port = environment.getRequiredProperty("local.server.port")
        val builder = HttpRequest.newBuilder(URI("http://127.0.0.1:$port$path"))
        if (body != null) {
            builder.header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.discarding()).statusCode()
    }

    @Test
    fun `blank and malformed sign-in tokens retain bad request through error dispatch`() {
        assertEquals(400, request("/api/auth/google", """{"idToken":""}"""))
        assertEquals(400, request("/api/auth/google", "{"))
    }

    @Test
    fun `direct requests to protected and error routes still require authentication`() {
        assertEquals(401, request("/api/auth-debug"))
        assertEquals(401, request("/error"))
    }
}
