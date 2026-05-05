package ru.underfish.communityservice.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.underfish.communityservice.database.dao.CommunityRepository
import ru.underfish.communityservice.database.entities.Community
import ru.underfish.communityservice.database.entities.enums.CommunityStatus
import ru.underfish.communityservice.dto.request.CommunityRequest
import ru.underfish.communityservice.dto.response.CommunityPageResponse
import ru.underfish.communityservice.dto.response.CommunityResponse
import ru.underfish.communityservice.dto.response.PageMeta
import ru.underfish.communityservice.exception.ForbiddenException
import ru.underfish.communityservice.exception.NotFoundException
import ru.underfish.communityservice.security.CurrentUserProvider
import java.util.UUID

/**
 * Community Service - управление сообществами
 *
 * SECURITY: ВСЕ методы проверяют userId из X-User-Id заголовка, переданного Gateway:
 * - Gateway проверяет JWT токен
 * - Gateway парсит userId из токена
 * - Gateway передаёт X-User-Id заголовок в микросервис
 * - GatewayHeaderAuthenticationFilter парсит заголовок и создаёт GatewayPrincipal
 * - CurrentUserProvider возвращает этот principal
 *
 * DATABASE: Логика создания сообщества:
 * 1. Пользователь приходит с JWT токеном
 * 2. Gateway извлекает userId из токена и отправляет X-User-Id
 * 3. Микросервис читает userId из заголовка
 * 4. CurrentUserProvider.getRequired() возвращает GatewayPrincipal с userId
 * 5. Сохраняем userId как organizerId в таблицу communities
 * 6. Пользователь автоматически становится организатором сообщества
 */
@Service
class CommunityService(
    private val communityRepository: CommunityRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val communityOrganizerService: ru.underfish.communityservice.service.CommunityOrganizerService,
) {
    /**
     * Создание сообщества
     * Текущий пользователь автоматически становится организатором
     *
     * FLOW:
     * - Клиент отправляет POST /api/v1/communities с JWT токеном
     * - Gateway проверяет JWT и передаёт X-User-Id
     * - CurrentUserProvider.getRequired() получает userId из SecurityContext
     * - Сохраняем Community с organizerId = текущий userId
     * - Пользователь создал сообщество и он уже организатор
     */
    fun createCommunity(request: CommunityRequest): CommunityResponse {
        val organizerId = currentUserProvider.getRequiredUserUuid()

        val community =
            Community().apply {
                name = request.name
                description = request.description
                isPrivate = request.isPrivate
                coverUrl = request.coverUrl
                this.organizerId = organizerId
                status = CommunityStatus.ACTIVE
            }

        val saved = communityRepository.save(community)

        // create organizer membership (current user) as ORGANIZER
        try {
            communityOrganizerService.addOrganizer(saved.communityId, organizerId)
        } catch (e: Exception) {
            // Log failure but do not fail community creation
            logger.warn("Failed to create organizer record for community=${saved.communityId}", e)
        }

        return mapToResponse(saved)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CommunityService::class.java)
    }

    /**
     * Получить одно сообщество по ID
     */
    fun getCommunity(communityId: UUID): CommunityResponse {
        val community =
            communityRepository.findByCommunityId(communityId)
                ?: throw NotFoundException("Community not found: $communityId")
        return mapToResponse(community)
    }

    /**
     * Получить все сообщества с фильтрацией, поиском и пагинацией
     *
     * API параметры:
     * - name: поиск по названию (LIKE)
     * - isPrivate: фильтр по типу (true/false)
     * - page: номер страницы (начиная с 0)
     * - size: размер страницы
     * - sort: поле сортировки (name, createdAt и т.д.)
     * - sortDirection: asc/desc
     *
     * Версия 2 добавит фильтр по тегам
     */
    @Suppress("LongMethod")
    fun getAllCommunities(
        name: String? = null,
        isPrivate: Boolean? = null,
        page: Int = 0,
        size: Int = 20,
        sort: String = "createdAt",
        sortDirection: String = "desc",
    ): CommunityPageResponse {
        // Создаём PageRequest с сортировкой
        val direction =
            if (sortDirection.equals("asc", ignoreCase = true)) {
                Sort.Direction.ASC
            } else {
                Sort.Direction.DESC
            }
        val pageRequest = PageRequest.of(page, size, Sort.by(direction, sort))

        // Получаем все сообщества и фильтруем в памяти
        // В реальной системе нужно использовать Specification или @Query
        val allCommunities = communityRepository.findAll()

        // Применяем фильтры
        var filtered = allCommunities
        if (!name.isNullOrBlank()) {
            filtered = filtered.filter { it.name.contains(name, ignoreCase = true) }
        }
        if (isPrivate != null) {
            filtered = filtered.filter { it.isPrivate == isPrivate }
        }

        // Сортируем
        val sortedList =
            when {
                sort.equals("name", ignoreCase = true) -> {
                    if (direction == Sort.Direction.ASC) {
                        filtered.sortedBy { it.name }
                    } else {
                        filtered.sortedByDescending { it.name }
                    }
                }

                sort.equals("createdAt", ignoreCase = true) -> {
                    if (direction == Sort.Direction.ASC) {
                        filtered.sortedBy { it.createdAt }
                    } else {
                        filtered.sortedByDescending { it.createdAt }
                    }
                }

                else -> {
                    if (direction == Sort.Direction.ASC) {
                        filtered.sortedBy { it.createdAt }
                    } else {
                        filtered.sortedByDescending { it.createdAt }
                    }
                }
            }

        // Пагинация
        val startIdx = page * size
        val endIdx = minOf(startIdx + size, sortedList.size)
        val pageContent = sortedList.subList(startIdx, endIdx)

        // Создаём Page объект
        val pageImpl =
            PageImpl(
                pageContent.map { mapToResponse(it) },
                pageRequest,
                sortedList.size.toLong(),
            )

        return CommunityPageResponse(
            content = pageImpl.content,
            meta =
                PageMeta(
                    page = pageImpl.number,
                    size = pageImpl.size,
                    totalElements = pageImpl.totalElements.toInt(),
                    totalPages = pageImpl.totalPages,
                ),
        )
    }

    /**
     * Обновить сообщество
     * Только организатор или администратор могут обновлять
     *
     * SECURITY CHECK:
     * - Получаем текущего пользователя из X-User-Id
     * - Проверяем что он либо организатор, либо админ
     * - Иначе выбрасываем ForbiddenException
     */
    fun updateCommunity(
        communityId: UUID,
        request: CommunityRequest,
    ): CommunityResponse {
        val community =
            communityRepository.findByCommunityId(communityId)
                ?: throw NotFoundException("Community not found: $communityId")

        val currentUserId = currentUserProvider.getRequiredUserUuid()

        // Проверка прав доступа
        val isOrganizer = community.organizerId == currentUserId
        val isAdmin = currentUserProvider.isAdmin()

        if (!isOrganizer && !isAdmin) {
            throw ForbiddenException("Only organizer or admin can update community")
        }

        community.apply {
            name = request.name
            description = request.description
            isPrivate = request.isPrivate
            coverUrl = request.coverUrl
        }

        val updated = communityRepository.save(community)
        return mapToResponse(updated)
    }

    /**
     * Удалить сообщество (soft delete - меняем статус на ARCHIVED)
     *
     * SECURITY CHECK:
     * - Получаем текущего пользователя из X-User-Id
     * - Проверяем что он либо организатор, либо админ
     */
    fun deleteCommunity(communityId: UUID) {
        val community =
            communityRepository.findByCommunityId(communityId)
                ?: throw NotFoundException("Community not found: $communityId")

        val currentUserId = currentUserProvider.getRequiredUserUuid()

        val isOrganizer = community.organizerId == currentUserId
        val isAdmin = currentUserProvider.isAdmin()

        if (!isOrganizer && !isAdmin) {
            throw ForbiddenException("Only organizer or admin can delete community")
        }

        // Soft delete - помечаем как удалённое
        community.status = CommunityStatus.ARCHIVED
        communityRepository.save(community)
    }

    private fun mapToResponse(community: Community): CommunityResponse {
        return CommunityResponse(
            communityId = community.communityId,
            name = community.name,
            description = community.description,
            organizerId = community.organizerId,
            isPrivate = community.isPrivate,
            coverUrl = community.coverUrl,
            createdAt = community.createdAt,
            updatedAt = community.updatedAt,
            status = community.status.name,
        )
    }
}
