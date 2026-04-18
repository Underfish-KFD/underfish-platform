package ru.underfish.abstractservice

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableFeignClients
class AbstractServiceApplication

fun main(args: Array<String>) {
    runApplication<AbstractServiceApplication>(*args)
}
