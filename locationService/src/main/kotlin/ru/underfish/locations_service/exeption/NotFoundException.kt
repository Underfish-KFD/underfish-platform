package ru.underfish.locations_service.exeption

// Исключение для случаев, когда ресурс не найден
class NotFoundException(message: String) : RuntimeException(message)