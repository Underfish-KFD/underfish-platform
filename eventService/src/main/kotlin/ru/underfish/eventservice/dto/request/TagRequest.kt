package ru.underfish.eventservice.dto.request

import jakarta.validation.constraints.NotBlank

data class TagRequest(
    @field:NotBlank
    val name: String,
)
