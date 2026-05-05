package ru.underfish.file_storage_service.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import ru.underfish.file_storage_service.domain.entity.BaseEntity
import java.util.UUID

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, UUID>
