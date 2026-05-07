package ru.underfish.file_storage_service.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "stored_files")
class StoredFile(
    id: UUID = UUID.randomUUID(),
    @Column(name = "original_name", nullable = false, length = 500)
    var originalName: String,
    @Column(name = "object_key", nullable = false, length = 500)
    var objectKey: String,
    @Column(name = "content_type", nullable = false, length = 100)
    var contentType: String,
    @Column(name = "size_bytes", nullable = false)
    var sizeBytes: Long,
    createdAt: LocalDateTime = LocalDateTime.now(),
    createdBy: String? = null,
    createdById: UUID? = null,
    updatedAt: LocalDateTime = LocalDateTime.now(),
    updatedBy: String? = null,
    updatedById: UUID? = null,
) : BaseEntity(id, createdAt, updatedAt, createdBy, createdById, updatedBy, updatedById)
