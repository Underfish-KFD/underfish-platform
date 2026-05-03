package ru.underfish.file_storage_service.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class StoredFileResponse(
    val id: UUID,
    val url: String,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val entityType: String,
    val entityId: UUID?,
    val uploadedBy: String,
    val createdAt: LocalDateTime
)
