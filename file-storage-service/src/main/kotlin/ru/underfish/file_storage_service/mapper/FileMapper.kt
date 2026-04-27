package ru.underfish.file_storage_service.mapper

import ru.underfish.file_storage_service.domain.entity.StoredFile
import ru.underfish.file_storage_service.dto.response.StoredFileResponse

fun StoredFile.toResponse() =
    StoredFileResponse(
        id = this.id,
        originalName = this.originalName,
        contentType = this.contentType,
        sizeBytes = this.sizeBytes,
        createdAt = this.createdAt,
    )
