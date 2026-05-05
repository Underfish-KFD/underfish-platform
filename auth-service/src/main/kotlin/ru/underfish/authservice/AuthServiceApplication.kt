package ru.underfish.authservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.EnableFeignClients
import ru.underfish.authservice.client.config.ProfileClientProperties

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(ProfileClientProperties::class)
class AuthServiceApplication

fun main(args: Array<String>) {
    runApplication<AuthServiceApplication>(*args)
}
