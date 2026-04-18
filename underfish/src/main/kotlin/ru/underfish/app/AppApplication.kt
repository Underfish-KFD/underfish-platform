package ru.underfish.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AppApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<AppApplication>(*args)
}
