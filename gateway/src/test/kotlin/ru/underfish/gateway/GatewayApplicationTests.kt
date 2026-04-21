package ru.underfish.gateway

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayApplicationTests {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun contextLoads() {
    }

    @Test
    fun healthIsPublic() {
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun apiV1RequiresAuthenticationWithoutToken() {
        webTestClient.get()
            .uri("/api/v1/events")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
