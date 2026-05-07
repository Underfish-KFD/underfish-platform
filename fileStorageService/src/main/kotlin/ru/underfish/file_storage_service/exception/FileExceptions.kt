package ru.underfish.file_storage_service.exception

/**GlobalException
 * Файл не найден
 */
class FileNotFoundException(
    message: String,
) : GlobalException(message)

/**
 * Ошибка при загрузке файла
 */
class FileUploadException(
    message: String,
    cause: Throwable? = null,
) : GlobalException(message, cause)
