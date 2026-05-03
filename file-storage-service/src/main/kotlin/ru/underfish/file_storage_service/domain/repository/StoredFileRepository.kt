package ru.underfish.file_storage_service.domain.repository


import org.springframework.data.jpa.repository.JpaRepository
import ru.underfish.file_storage_service.domain.entity.StoredFile
import java.util.UUID

interface StoredFileRepository : JpaRepository<StoredFile, UUID> {
    fun findByEntityTypeAndEntityId(
        entityType: String,
        entityId: UUID
    ): List<StoredFile>
}
