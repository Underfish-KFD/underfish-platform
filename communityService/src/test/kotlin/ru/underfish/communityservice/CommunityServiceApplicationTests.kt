package ru.underfish.communityservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["auth.service.url=http://localhost:8080"]) // provide dummy URL for Feign client in tests
class CommunityServiceApplicationTests {
    @Test
    fun contextLoads() {
        // Test passes if Spring context loads successfully
    }
}
