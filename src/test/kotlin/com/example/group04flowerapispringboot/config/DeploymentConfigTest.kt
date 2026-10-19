package com.example.group04flowerapispringboot.config

import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeploymentConfigTest {
    private fun deploymentEnvironment() = MockEnvironment().apply {
        setActiveProfiles("deploy")
        for (name in listOf("PGHOST", "PGPORT", "PGDATABASE", "PGUSER", "PGPASSWORD", "GOOGLE_WEB_CLIENT_ID")) {
            setProperty(name, "test-value")
        }
    }

    @Test
    fun `accepts explicit deployment settings`() {
        DeploymentConfig(deploymentEnvironment())
    }

    @Test
    fun `rejects missing deployment secrets without exposing values`() {
        val environment = deploymentEnvironment()
        environment.propertySources.remove("mockProperties")
        val error = assertFailsWith<IllegalArgumentException> { DeploymentConfig(environment) }
        assertTrue(error.message.orEmpty().contains("PGHOST"))
    }

    @Test
    fun `rejects blank passwords`() {
        val environment = deploymentEnvironment().withProperty("PGPASSWORD", " ")
        val error = assertFailsWith<IllegalArgumentException> { DeploymentConfig(environment) }
        assertTrue(error.message.orEmpty().contains("PGPASSWORD"))
    }

    @Test
    fun `rejects local and deployment profiles together`() {
        val environment = deploymentEnvironment().apply { setActiveProfiles("local", "deploy") }
        assertFailsWith<IllegalArgumentException> { DeploymentConfig(environment) }
    }
}
