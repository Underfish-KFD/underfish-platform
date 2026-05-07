package ru.underfish.file_storage_service.exception

/**
 * Базовое исключение для всех ошибок приложения
 */
open class GlobalException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
