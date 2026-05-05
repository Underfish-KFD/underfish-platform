package ru.underfish.file_storage_service.service

import io.minio.*
import io.minio.http.Method
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import  ru.underfish.file_storage_service.config.MinioProperties
import  ru.underfish.file_storage_service.exception.FileUploadException
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class MinioServiceImpl(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties,
) : MinioService {
    @PostConstruct
    fun ensureBucketExists() {
        val exists =
            minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(minioProperties.bucket).build(),
            )
        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(minioProperties.bucket).build(),
            )
        }
    }

    override fun upload(file: MultipartFile): String {
        val objectKey = "${UUID.randomUUID()}/${file.originalFilename}"
        try {
            minioClient.putObject(
                PutObjectArgs
                    .builder()
                    .bucket(minioProperties.bucket)
                    .`object`(objectKey)
                    .stream(file.inputStream, file.size, -1)
                    .contentType(file.contentType ?: "application/octet-stream")
                    .build(),
            )
        } catch (e: Exception) {
            throw FileUploadException("Ошибка загрузки файла: ${e.message}", e)
        }
        return objectKey
    }

    override fun uploadText(
        objectKey: String,
        content: String,
    ) {
        try {
            val bytes = content.toByteArray(Charsets.UTF_8)
            minioClient.putObject(
                PutObjectArgs
                    .builder()
                    .bucket(minioProperties.bucket)
                    .`object`(objectKey)
                    .stream(ByteArrayInputStream(bytes), bytes.size.toLong(), -1)
                    .contentType("text/plain; charset=utf-8")
                    .build(),
            )
        } catch (e: Exception) {
            throw FileUploadException("Ошибка сохранения diff: ${e.message}", e)
        }
    }

    override fun getPresignedUrl(objectKey: String): String =
        minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs
                .builder()
                .bucket(minioProperties.bucket)
                .`object`(objectKey)
                .method(Method.GET)
                .expiry(1, TimeUnit.HOURS)
                .build(),
        )

    override fun getObject(objectKey: String): InputStream =
        minioClient.getObject(
            GetObjectArgs
                .builder()
                .bucket(minioProperties.bucket)
                .`object`(objectKey)
                .build(),
        )

    override fun delete(objectKey: String) {
        minioClient.removeObject(
            RemoveObjectArgs
                .builder()
                .bucket(minioProperties.bucket)
                .`object`(objectKey)
                .build(),
        )
    }

    override fun streamFile(
        objectKey: String,
        outputStream: OutputStream,
    ) {
        minioClient
            .getObject(
                GetObjectArgs
                    .builder()
                    .bucket(minioProperties.bucket)
                    .`object`(objectKey)
                    .build(),
            ).use { stream ->
                stream.copyTo(outputStream)
            }
    }
}
