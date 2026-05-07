package ru.underfish.file_storage_service.config

import io.minio.MinioClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    val endpoint: String = "http://localhost:9000",
    val accessKey: String = "minioadmin",
    val secretKey: String = "minioadmin",
    val bucket: String = "underfish-files",
)

@Configuration
class MinioConfig(
    private val props: MinioProperties,
) {
    @Bean
    fun minioClient(): MinioClient =
        MinioClient
            .builder()
            .endpoint(props.endpoint)
            .credentials(props.accessKey, props.secretKey)
            .build()
}
