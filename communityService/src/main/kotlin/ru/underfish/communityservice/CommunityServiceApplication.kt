package ru.underfish.communityservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class CommunityServiceApplication

fun main(args: Array<String>) {
    runApplication<CommunityServiceApplication>(args = args)
}
