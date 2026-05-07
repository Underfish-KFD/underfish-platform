package ru.underfish.file_storage_service.service

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.io.OutputStream

interface MinioService {
    fun upload(file: MultipartFile): String // возвращает objectKey

    fun uploadText(
        objectKey: String,
        content: String,
    )

    fun getPresignedUrl(objectKey: String): String

    fun getObject(objectKey: String): InputStream

    fun delete(objectKey: String)

    fun streamFile(
        objectKey: String,
        outputStream: OutputStream,
    )
}
