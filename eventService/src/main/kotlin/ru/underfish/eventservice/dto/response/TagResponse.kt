package ru.underfish.eventservice.dto.response

import ru.underfish.eventservice.database.entities.Tag
import java.util.UUID

data class TagResponse(
    val tagId: UUID,
    val name: String,
) {
    constructor(tag: Tag) : this(
        tagId = tag.id ?: UUID.randomUUID(),
        name = tag.name,
    )
}

