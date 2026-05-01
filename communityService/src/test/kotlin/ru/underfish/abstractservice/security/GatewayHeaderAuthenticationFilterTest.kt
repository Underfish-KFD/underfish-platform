package ru.underfish.abstractservice.security

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(
    classes = [TestSecurityApplication::class],
    properties = [
        "gateway.security.trusted-internal-token=test-internal-token",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GatewayHeaderAuthenticationFilterTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `returns unauthorized when identity headers are missing`() {
        mockMvc.perform(get("/internal/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `returns unauthorized when internal token is invalid`() {
        mockMvc.perform(
            get("/internal/me")
                .header("X-User-Id", "user-1")
                .header("X-User-Roles", "USER")
                .header("X-Internal-Token", "wrong-token")
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `authorizes request when gateway headers are valid`() {
        mockMvc.perform(
            get("/internal/me")
                .header("X-User-Id", "user-42")
                .header("X-User-Roles", "USER,ADMIN")
                .header("X-Internal-Token", "test-internal-token")
                .header("X-Auth-Source", "gateway")
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(content().string("user-42"))
    }
}

