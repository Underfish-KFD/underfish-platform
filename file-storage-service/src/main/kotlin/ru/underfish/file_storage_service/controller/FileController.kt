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
import ru.underfish.file_storage_service.service.MinioService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val minioService: MinioService,
    private val fileRepository: StoredFileRepository,
) {
    @PostMapping("/upload")
    fun upload(
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<StoredFileResponse> {
        val objectKey = minioService.upload(file)
        val stored =
            fileRepository.save(
                StoredFile(
                    originalName = file.originalFilename ?: "unknown",
                    objectKey = objectKey,
                    contentType = file.contentType ?: "application/octet-stream",
                    sizeBytes = file.size
                ),
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(stored.toResponse())
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
    ): StoredFileResponse =
        fileRepository
            .findById(id)
            .orElseThrow { FileNotFoundException("Файл $id не найден") }
            .toResponse()
}
