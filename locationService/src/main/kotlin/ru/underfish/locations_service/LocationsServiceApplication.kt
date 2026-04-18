package ru.underfish.locations_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LocationsServiceApplication

fun main(args: Array<String>) {
	runApplication<LocationsServiceApplication>(*args)
}
