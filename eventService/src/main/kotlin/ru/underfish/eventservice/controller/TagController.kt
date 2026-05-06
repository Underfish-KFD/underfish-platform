package ru.underfish.eventservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
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
import ru.underfish.eventservice.exception.BadRequestException
import ru.underfish.eventservice.service.TagService
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID

@RestController
@RequestMapping("/api/v1/tags")
class TagController(
    private val tagService: TagService,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTag(
        @RequestBody body: ByteArray,
    ): TagResponse = tagService.createTag(parseTagRequest(body))

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
        @RequestBody body: ByteArray,
    ): TagResponse = tagService.updateTag(tagId, parseTagRequest(body))

    @DeleteMapping("/{tag_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTag(
        @PathVariable("tag_id") tagId: UUID,
    ) {
        tagService.deleteTag(tagId)
    }

    private fun parseTagRequest(body: ByteArray): TagRequest =
        objectMapper.readValue(body.decodeJsonBody(), TagRequest::class.java)

    private fun ByteArray.decodeJsonBody(): String =
        runCatching { StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(this)).toString() }
            .recoverCatching { error ->
                if (error !is CharacterCodingException) {
                    throw error
                }
                String(this, WINDOWS_1251)
            }
            .getOrElse { throw BadRequestException("Invalid request body encoding") }

    companion object {
        private val WINDOWS_1251: Charset = Charset.forName("windows-1251")
    }
}
