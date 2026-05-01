package ru.underfish.file_storage_service.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class StoredFileResponse(
    val id: UUID,
    val originalName: String,
    val contentType: String,
    val sizeBytes: Long,
    val createdAt: LocalDateTime,
)
