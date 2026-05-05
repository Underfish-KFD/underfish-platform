package ru.underfish.eventservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class EventServiceApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<EventServiceApplication>(*args)
}
