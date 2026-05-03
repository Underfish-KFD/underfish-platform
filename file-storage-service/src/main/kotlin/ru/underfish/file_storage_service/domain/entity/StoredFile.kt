package ru.underfish.file_storage_service.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "stored_files")
class StoredFile(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "original_name", nullable = false, length = 500)
    var originalName: String,
    @Column(name = "object_key", nullable = false, length = 500, unique = true)
    var objectKey: String,
    @Column(name = "content_type", nullable = false, length = 100)
    var contentType: String,
    @Column(name = "size_bytes", nullable = false)
    var sizeBytes: Long,
    @Column(name = "entity_type", nullable = false, length = 50)
    var entityType: String,
    @Column(name = "entity_id")
    var entityId: UUID?,
    @Column(name = "uploaded_by", nullable = false)
    var uploadedBy: String,
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),
    )
