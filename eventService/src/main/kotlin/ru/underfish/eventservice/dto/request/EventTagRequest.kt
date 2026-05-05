package ru.underfish.eventservice.dto.request

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class EventTagRequest(
    @field:NotNull
    val tagId: UUID,
)
