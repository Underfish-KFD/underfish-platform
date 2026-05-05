package ru.underfish.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GatewayApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<GatewayApplication>(*args)
}
