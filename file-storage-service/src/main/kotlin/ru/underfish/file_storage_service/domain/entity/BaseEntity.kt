package ru.underfish.file_storage_service.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID = UUID.randomUUID(),
    @Column(name = "created_at", nullable = false, updatable = false)
    open val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "created_by")
    open var createdBy: String? = null,
    @Column(name = "created_by_id")
    open var createdById: UUID? = null,
    @Column(name = "updated_by")
    open var updatedBy: String? = null,
    @Column(name = "updated_by_id")
    open var updatedById: UUID? = null,
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
