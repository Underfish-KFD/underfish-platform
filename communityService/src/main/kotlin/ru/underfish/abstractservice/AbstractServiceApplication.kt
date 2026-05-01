package ru.underfish.abstractservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class AbstractServiceApplication

fun main(args: Array<String>) {
    runApplication<AbstractServiceApplication>(*args)
}
