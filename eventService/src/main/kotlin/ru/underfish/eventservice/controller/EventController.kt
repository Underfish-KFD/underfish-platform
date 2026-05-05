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
import ru.underfish.eventservice.dto.filter.EventSearchFilter
import ru.underfish.eventservice.dto.request.EventRequest
import ru.underfish.eventservice.dto.request.EventTagRequest
import ru.underfish.eventservice.dto.response.EventPageResponse
import ru.underfish.eventservice.dto.response.EventResponse
import ru.underfish.eventservice.dto.response.EventTagResponse
import ru.underfish.eventservice.security.CurrentUserProvider
import ru.underfish.eventservice.service.EventService
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventService: EventService,
    private val currentUserProvider: CurrentUserProvider,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEvent(
        @Valid @RequestBody request: EventRequest,
    ): EventResponse {
        val userId = currentUserProvider.getRequiredUserUuid()
        return eventService.createEvent(request, userId)
    }

    @GetMapping
    fun getEvents(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) is_online: Boolean?,
        @RequestParam(required = false) start_from: LocalDateTime?,
        @RequestParam(required = false) start_to: LocalDateTime?,
        @RequestParam(required = false) price_min: Double?,
        @RequestParam(required = false) price_max: Double?,
        @RequestParam(required = false) location_id: UUID?,
        @RequestParam(required = false) tag_ids: List<UUID>?,
        @RequestParam(required = false) community_id: UUID?,
        @RequestParam(required = false) organizer_id: UUID?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "startDatetime") sort: String,
        @RequestParam(defaultValue = "asc") sort_direction: String,
    ): EventPageResponse =
        eventService.getEvents(
            filter =
                EventSearchFilter(
                    title = title,
                    status = status,
                    isOnline = is_online,
                    startFrom = start_from,
                    startTo = start_to,
                    priceMin = price_min,
                    priceMax = price_max,
                    locationId = location_id,
                    tagIds = tag_ids.orEmpty(),
                    communityId = community_id,
                    organizerId = organizer_id,
                ),
            page = page,
            size = size,
            sortField = sort,
            sortDirection = sort_direction,
        )

    @GetMapping("/{event_id}")
    fun getEventById(
        @PathVariable("event_id") eventId: UUID,
    ): EventResponse = eventService.getEventById(eventId)

    @PutMapping("/{event_id}")
    fun updateEvent(
        @PathVariable("event_id") eventId: UUID,
        @Valid @RequestBody request: EventRequest,
    ): EventResponse = eventService.updateEvent(eventId, request)

    @DeleteMapping("/{event_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteEvent(
        @PathVariable("event_id") eventId: UUID,
    ) {
        eventService.deleteEvent(eventId)
    }

    @PostMapping("/{event_id}/tags")
    @ResponseStatus(HttpStatus.CREATED)
    fun addTagToEvent(
        @PathVariable("event_id") eventId: UUID,
        @Valid @RequestBody request: EventTagRequest,
    ): EventTagResponse = eventService.addTagToEvent(eventId, request)

    @GetMapping("/{event_id}/tags")
    fun getEventTags(
        @PathVariable("event_id") eventId: UUID,
    ): List<EventTagResponse> = eventService.getEventTags(eventId)

    @DeleteMapping("/{event_id}/tags/{tag_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeTagFromEvent(
        @PathVariable("event_id") eventId: UUID,
        @PathVariable("tag_id") tagId: UUID,
    ) {
        eventService.removeTagFromEvent(eventId, tagId)
    }
}

