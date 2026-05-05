package ru.underfish.eventservice.dto.request

import jakarta.validation.constraints.NotBlank

data class EventAttendanceUpdateRequest(
    @field:NotBlank
    val status: String,
)
