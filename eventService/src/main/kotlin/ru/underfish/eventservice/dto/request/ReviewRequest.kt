package ru.underfish.eventservice.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class ReviewRequest(
    @field:Min(1)
    @field:Max(5)
    val rating: Int,
    @field:NotBlank
    val comment: String,
)
