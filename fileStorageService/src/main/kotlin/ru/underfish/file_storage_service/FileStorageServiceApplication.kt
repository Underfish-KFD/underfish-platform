package ru.underfish.file_storage_service

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
class FileStorageServiceApplication

fun main(args: Array<String>) {
    runApplication<FileStorageServiceApplication>(*args)
}
