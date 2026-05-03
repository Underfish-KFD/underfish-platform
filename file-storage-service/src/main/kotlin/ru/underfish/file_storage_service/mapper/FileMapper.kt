package ru.underfish.file_storage_service.mapper

import ru.underfish.file_storage_service.domain.entity.StoredFile
import ru.underfish.file_storage_service.dto.response.StoredFileResponse

fun StoredFile.toResponse(
    url: String
): StoredFileResponse =
    StoredFileResponse(
        id = this.id,
        url = url,
        fileName = this.originalName,
        contentType = this.contentType,
        sizeBytes = this.sizeBytes,
        entityType = this.entityType,
        entityId = this.entityId,
        uploadedBy = this.uploadedBy,
        createdAt = this.createdAt
    )
