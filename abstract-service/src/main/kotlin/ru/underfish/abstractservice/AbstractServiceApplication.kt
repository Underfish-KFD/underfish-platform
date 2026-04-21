package ru.underfish.abstractservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AbstractServiceApplication

fun main(args: Array<String>) {
    runApplication<AbstractServiceApplication>(*args)
}
