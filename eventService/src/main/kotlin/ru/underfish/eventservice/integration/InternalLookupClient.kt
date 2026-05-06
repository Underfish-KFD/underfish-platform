package ru.underfish.eventservice.integration

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.underfish.eventservice.exception.NotFoundException
import java.util.UUID

@Component
class InternalLookupClient(
    private val integrationProperties: IntegrationProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val profileClient = restClientBuilder.baseUrl(integrationProperties.profileServiceBaseUrl).build()
    private val locationClient = restClientBuilder.baseUrl(integrationProperties.locationServiceBaseUrl).build()

    fun ensureUserExists(userId: UUID) {
        if (!integrationProperties.validationEnabled) {
            return
        }
        callExists(profileClient, "/internal/users/$userId/exists", "User")
    }

    fun ensureLocationExists(locationId: String) {
        if (!integrationProperties.validationEnabled) {
            return
        }
        callExists(locationClient, "/internal/locations/$locationId/exists", "Location")
    }

    private fun callExists(
        client: RestClient,
        path: String,
        entityName: String,
    ) {
        val result =
            client.get()
                .uri(path)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { _, _ ->
                    throw NotFoundException("$entityName not found")
                }
                .toBodilessEntity()

        if (!result.statusCode.is2xxSuccessful) {
            log.warn("Unexpected status for {} lookup: {}", entityName, result.statusCode)
            throw NotFoundException("$entityName not found")
        }
    }
}

