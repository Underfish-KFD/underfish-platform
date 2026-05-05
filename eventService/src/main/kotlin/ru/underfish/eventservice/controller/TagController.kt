package ru.underfish.eventservice.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.underfish.eventservice.dto.request.TagRequest
import ru.underfish.eventservice.dto.response.TagResponse
import ru.underfish.eventservice.service.TagService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/tags")
class TagController(
    private val tagService: TagService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTag(
        @Valid @RequestBody request: TagRequest,
    ): TagResponse = tagService.createTag(request)

    @GetMapping
    fun getTags(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ): List<TagResponse> = tagService.getTags(page, size)

    @GetMapping("/{tag_id}")
    fun getTag(
        @PathVariable("tag_id") tagId: UUID,
    ): TagResponse = tagService.getTagById(tagId)

    @PutMapping("/{tag_id}")
    fun updateTag(
        @PathVariable("tag_id") tagId: UUID,
        @Valid @RequestBody request: TagRequest,
    ): TagResponse = tagService.updateTag(tagId, request)

    @DeleteMapping("/{tag_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTag(
        @PathVariable("tag_id") tagId: UUID,
    ) {
        tagService.deleteTag(tagId)
    }
}
