package ru.underfish.file_storage_service.controller


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.underfish.file_storage_service.domain.entity.StoredFile
import ru.underfish.file_storage_service.domain.repository.StoredFileRepository
import ru.underfish.file_storage_service.dto.response.StoredFileResponse
import ru.underfish.file_storage_service.exception.FileNotFoundException
import ru.underfish.file_storage_service.mapper.toResponse
import ru.underfish.file_storage_service.security.CurrentUserProvider
import ru.underfish.file_storage_service.service.MinioService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val minioService: MinioService,
    private val fileRepository: StoredFileRepository,
    private val currentUserProvider: CurrentUserProvider,
) {
    @PostMapping("/upload")
    fun upload(
        @RequestParam file: MultipartFile,
        @RequestParam entityType: String,
        @RequestParam(required = false) entityId: UUID?
    ): ResponseEntity<StoredFileResponse> {
        val user = currentUserProvider.getRequired()

        val objectKey = minioService.upload(file)

        val saved = fileRepository.save(
            StoredFile(
                originalName = file.originalFilename ?: "unknown",
                objectKey = objectKey,
                contentType = file.contentType ?: "application/octet-stream",
                sizeBytes = file.size,
                entityType = entityType,
                entityId = entityId,
                uploadedBy = user.userId
            )
        )

        val url = minioService.getPresignedUrl(objectKey)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            StoredFileResponse(
                id = saved.id,
                url = url,
                fileName = saved.originalName,
                contentType = saved.contentType,
                sizeBytes = saved.sizeBytes,
                entityType = saved.entityType,
                entityId = saved.entityId,
                uploadedBy = saved.uploadedBy,
                createdAt = saved.createdAt
            )
        )
    }

    @GetMapping("/{fileId}")
    fun getById(
        @PathVariable fileId: UUID,
    ): StoredFileResponse {
        val file = fileRepository.findById(fileId)
            .orElseThrow { RuntimeException("File not found") }

        return StoredFileResponse(
            id = file.id,
            url = minioService.getPresignedUrl(file.objectKey),
            fileName = file.originalName,
            contentType = file.contentType,
            sizeBytes = file.sizeBytes,
            entityType = file.entityType,
            entityId = file.entityId,
            uploadedBy = file.uploadedBy,
            createdAt = file.createdAt
        )
    }

    @GetMapping
    fun getByEntity(
        @RequestParam entityType: String,
        @RequestParam entityId: UUID
    ): List<StoredFileResponse> {

        //TODO: Разобраться с entity type

        //TODO: Нужна - ли такая проверка?
        //currentUserProvider.requireSelfOrAdmin(entityId.toString())

        return fileRepository.findByEntityTypeAndEntityId(entityType, entityId)
            .map {
                StoredFileResponse(
                    id = it.id,
                    url = minioService.getPresignedUrl(it.objectKey),
                    fileName = it.originalName,
                    contentType = it.contentType,
                    sizeBytes = it.sizeBytes,
                    entityType = it.entityType,
                    entityId = it.entityId,
                    uploadedBy = it.uploadedBy,
                    createdAt = it.createdAt
                )
            }
    }

    @DeleteMapping("/{fileId}")
    fun delete(@PathVariable fileId: UUID) {

        val user = currentUserProvider.getRequired()

        val file = fileRepository.findById(fileId)
            .orElseThrow { RuntimeException("File not found") }

        if (file.uploadedBy != user.userId && !currentUserProvider.isAdmin()) {
            throw RuntimeException("Forbidden")
        }

        minioService.delete(file.objectKey)
        fileRepository.delete(file)
    }
}
