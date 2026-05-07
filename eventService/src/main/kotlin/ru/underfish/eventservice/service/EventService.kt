package ru.underfish.eventservice.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.underfish.eventservice.database.dao.EventRepository
import ru.underfish.eventservice.database.dao.TagRepository
import ru.underfish.eventservice.database.entities.Event
import ru.underfish.eventservice.database.entities.enums.EventStatus
import ru.underfish.eventservice.dto.filter.EventSearchFilter
import ru.underfish.eventservice.dto.request.EventRequest
import ru.underfish.eventservice.dto.request.EventTagRequest
import ru.underfish.eventservice.dto.response.EventPageResponse
import ru.underfish.eventservice.dto.response.EventResponse
import ru.underfish.eventservice.dto.response.EventTagResponse
import ru.underfish.eventservice.dto.response.PageMeta
import ru.underfish.eventservice.exception.BadRequestException
import ru.underfish.eventservice.exception.NotFoundException
import ru.underfish.eventservice.integration.InternalLookupClient
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val tagRepository: TagRepository,
    private val internalLookupClient: InternalLookupClient,
) {
    @Transactional
    fun createEvent(
        request: EventRequest,
        organizerId: UUID,
    ): EventResponse {
        internalLookupClient.ensureUserExists(organizerId)
        internalLookupClient.ensureLocationExists(request.locationId)

        val event =
            Event(organizerId = organizerId, title = request.title).apply {
                description = request.description
                startDatetime = request.startDatetime
                endDatetime = request.endDatetime
                locationId = request.locationId
                price = request.price?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
                currency = request.currency ?: "RUB"
                posterUrl = request.posterUrl
                communityId = request.communityId
                eventStatus = parseEventStatus(request.status)
                updatedAt = LocalDateTime.now()
                maxParticipants = request.maxParticipants ?: 0
                isOnline = request.isOnline ?: false
                photoUrls = request.photoUrls?.toMutableList() ?: mutableListOf()
            }

        return EventResponse.fromEntity(eventRepository.save(event))
    }

    fun getEvents(
        filter: EventSearchFilter,
        page: Int,
        size: Int,
        sortField: String,
        sortDirection: String,
    ): EventPageResponse {
        val direction = if (sortDirection.equals("desc", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), Sort.by(direction, sortField))

        val spec =
            try {
                EventSpecifications.build(filter)
            } catch (_: IllegalArgumentException) {
                throw BadRequestException("Invalid event status")
            }

        val eventPage = eventRepository.findAll(spec, pageable)
        return EventPageResponse(
            content = eventPage.content.map(EventResponse::fromEntity),
            meta =
                PageMeta(
                    page = eventPage.number,
                    size = eventPage.size,
                    totalElements = eventPage.totalElements,
                    totalPages = eventPage.totalPages,
                ),
        )
    }

    fun getEventById(eventId: UUID): EventResponse = EventResponse.fromEntity(findEvent(eventId))

    @Transactional
    fun updateEvent(
        eventId: UUID,
        request: EventRequest,
    ): EventResponse {
        val event = findEvent(eventId)
        internalLookupClient.ensureLocationExists(request.locationId)

        event.title = request.title
        event.description = request.description
        event.startDatetime = request.startDatetime
        event.endDatetime = request.endDatetime
        event.locationId = request.locationId
        event.price = request.price?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
        event.currency = request.currency ?: "RUB"
        event.posterUrl = request.posterUrl
        event.eventStatus = parseEventStatus(request.status)
        event.maxParticipants = request.maxParticipants ?: 0
        event.isOnline = request.isOnline ?: false
        event.updatedAt = LocalDateTime.now()
        request.photoUrls?.let { event.photoUrls = it.toMutableList() }

        return EventResponse.fromEntity(eventRepository.save(event))
    }

    @Transactional
    fun deleteEvent(eventId: UUID) {
        eventRepository.delete(findEvent(eventId))
    }

    @Transactional
    fun addTagToEvent(
        eventId: UUID,
        request: EventTagRequest,
    ): EventTagResponse {
        val event = findEvent(eventId)
        val tag = tagRepository.findById(request.tagId).orElseThrow { NotFoundException("Tag not found") }
        event.tags.add(tag)
        eventRepository.save(event)
        return EventTagResponse(eventId = eventId, tagId = request.tagId)
    }

    @Transactional(readOnly = true)
    fun getEventTags(eventId: UUID): List<EventTagResponse> {
        val event = findEvent(eventId)
        return event.tags.map { tag -> EventTagResponse(eventId = eventId, tagId = tag.id ?: UUID.randomUUID()) }
    }

    @Transactional
    fun removeTagFromEvent(
        eventId: UUID,
        tagId: UUID,
    ) {
        val event = findEvent(eventId)
        val tag = tagRepository.findById(tagId).orElseThrow { NotFoundException("Tag not found") }
        event.tags.remove(tag)
        eventRepository.save(event)
    }

    private fun findEvent(eventId: UUID): Event =
        eventRepository.findById(eventId).orElseThrow { NotFoundException("Event not found") }

    private fun parseEventStatus(status: String): EventStatus =
        EventStatus.entries.firstOrNull { it.name.equals(status, ignoreCase = true) }
            ?: throw BadRequestException("Invalid event status")
}

