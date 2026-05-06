package ru.underfish.eventservice.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.underfish.eventservice.database.dao.TagRepository
import ru.underfish.eventservice.database.entities.Tag
import ru.underfish.eventservice.dto.request.TagRequest
import ru.underfish.eventservice.dto.response.TagResponse
import ru.underfish.eventservice.exception.BadRequestException
import ru.underfish.eventservice.exception.NotFoundException
import java.util.UUID

@Service
class TagService(
    private val tagRepository: TagRepository,
) {
    @Transactional
    fun createTag(request: TagRequest): TagResponse {
        val name = request.name.trim()
        if (name.isBlank()) {
            throw BadRequestException("Tag name must not be blank")
        }
        if (tagRepository.existsByName(name)) {
            throw BadRequestException("Tag with name '$name' already exists")
        }
        return TagResponse(tagRepository.save(Tag(name = name)))
    }

    fun getTags(
        page: Int,
        size: Int,
    ): List<TagResponse> =
        tagRepository.findAll(PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1))).content.map(::TagResponse)

    fun getTagById(tagId: UUID): TagResponse {
        val tag = tagRepository.findById(tagId).orElseThrow { NotFoundException("Tag not found") }
        return TagResponse(tag)
    }

    @Transactional
    fun updateTag(
        tagId: UUID,
        request: TagRequest,
    ): TagResponse {
        val tag = tagRepository.findById(tagId).orElseThrow { NotFoundException("Tag not found") }
        val name = request.name.trim()
        if (name.isBlank()) {
            throw BadRequestException("Tag name must not be blank")
        }
        if (tagRepository.existsByNameAndIdNot(name, tagId)) {
            throw BadRequestException("Tag with name '$name' already exists")
        }
        tag.name = name
        return TagResponse(tagRepository.save(tag))
    }

    @Transactional
    fun deleteTag(tagId: UUID) {
        if (!tagRepository.existsById(tagId)) {
            throw NotFoundException("Tag not found")
        }
        tagRepository.deleteById(tagId)
    }
}

