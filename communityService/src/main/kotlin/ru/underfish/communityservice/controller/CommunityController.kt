package ru.underfish.communityservice.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.underfish.communityservice.dto.request.CommunityRequest
import ru.underfish.communityservice.dto.response.CommunityPageResponse
import ru.underfish.communityservice.dto.response.CommunityResponse
import ru.underfish.communityservice.service.CommunityService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/communities")
class CommunityController(
    private val communityService: CommunityService,
) {
    /**
     * POST /api/v1/communities
     * Создать сообщество (authenticated)
     * Текущий пользователь становится организатором
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun createCommunity(
        @RequestBody request: CommunityRequest,
    ): ResponseEntity<CommunityResponse> {
        val response = communityService.createCommunity(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * GET /api/v1/communities
     * Получить все сообщества с пагинацией, фильтрацией и сортировкой
     *
     * Query параметры (per OpenAPI):
     * - name: поиск по названию (частичное совпадение, case-insensitive)
     * - is_private: фильтр по типу (true/false)
     * - tag_ids: фильтр по тегам (версия 2)
     * - page: номер страницы (default: 0)
     * - size: размер страницы (default: 20)
     * - sort: поле сортировки (default: createdAt)
     * - sort_direction: asc/desc (default: asc)
     */
    @GetMapping
    @PreAuthorize("permitAll")
    fun getAllCommunities(
        @RequestParam(required = false) name: String?,
        @RequestParam(value = "is_private", required = false) isPrivate: Boolean?,
        @RequestParam(value = "tag_ids", required = false) @Suppress("UNUSED_PARAMETER") tagIds: List<UUID>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "created_at") sort: String,
        @RequestParam(value = "sort_direction", defaultValue = "desc") sortDirection: String,
    ): ResponseEntity<CommunityPageResponse> {
        // tagIds parameter is accepted for API compatibility but not used in version 1
        val communities =
            communityService.getAllCommunities(
                name = name,
                isPrivate = isPrivate,
                page = page,
                size = size,
                sort = convertSortField(sort),
                sortDirection = sortDirection,
            )
        return ResponseEntity.ok(communities)
    }

    /**
     * GET /api/v1/communities/{community_id}
     * Получить одно сообщество по ID
     */
    @GetMapping("/{community_id}")
    @PreAuthorize("permitAll")
    fun getCommunity(
        @PathVariable("community_id") communityId: UUID,
    ): ResponseEntity<CommunityResponse> {
        val community = communityService.getCommunity(communityId)
        return ResponseEntity.ok(community)
    }

    /**
     * PUT /api/v1/communities/{community_id}
     * Обновить сообщество (только организатор или админ)
     */
    @PutMapping("/{community_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun updateCommunity(
        @PathVariable("community_id") communityId: UUID,
        @RequestBody request: CommunityRequest,
    ): ResponseEntity<CommunityResponse> {
        val response = communityService.updateCommunity(communityId, request)
        return ResponseEntity.ok(response)
    }

    /**
     * DELETE /api/v1/communities/{community_id}
     * Удалить сообщество (soft delete, только организатор или админ)
     */
    @DeleteMapping("/{community_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun deleteCommunity(
        @PathVariable("community_id") communityId: UUID,
    ): ResponseEntity<Void> {
        communityService.deleteCommunity(communityId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Helper для конвертации названия поля из OpenAPI в Kotlin
     * OpenAPI использует snake_case, Kotlin использует camelCase
     */
    private fun convertSortField(sortField: String): String {
        return when (sortField) {
            "created_at" -> "createdAt"
            "updated_at" -> "updatedAt"
            else -> sortField
        }
    }
}
