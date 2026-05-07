package ru.underfish.profile_service

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ProfileServiceApplicationTests {

    @Test
    fun contextLoads() {
    }

}
