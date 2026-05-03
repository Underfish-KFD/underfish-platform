package ru.underfish.eventservice.integration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "integration")
data class IntegrationProperties(
    var validationEnabled: Boolean = false,
    var profileServiceBaseUrl: String = "http://localhost:8084",
    var locationServiceBaseUrl: String = "http://localhost:8082",
)

