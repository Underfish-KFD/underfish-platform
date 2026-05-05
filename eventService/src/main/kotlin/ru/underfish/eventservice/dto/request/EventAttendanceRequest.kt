package ru.underfish.eventservice.dto.request

import jakarta.validation.constraints.NotBlank

data class EventAttendanceRequest(
    @field:NotBlank
    val status: String,
)
