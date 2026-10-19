package com.example.group04flowerapispringboot.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@Configuration(proxyBeanMethods = false)
@Profile("deploy")
class DeploymentConfig(environment: Environment) {
    init {
        require("local" !in environment.activeProfiles) {
            "Select either local or deploy, never both profiles."
        }
        // JDBC connections are lazy. Validate before serving requests, without
        // connecting to or modifying the deployment database.
        for (name in listOf("PGHOST", "PGPORT", "PGDATABASE", "PGUSER", "PGPASSWORD", "GOOGLE_WEB_CLIENT_ID")) {
            require(!environment.getProperty(name).isNullOrBlank()) {
                "Deployment requires environment variable $name."
            }
        }
    }
}
