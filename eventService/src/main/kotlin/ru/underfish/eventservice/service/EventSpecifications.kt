package ru.underfish.eventservice.service

import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification
import ru.underfish.eventservice.database.entities.Event
import ru.underfish.eventservice.database.entities.Tag
import ru.underfish.eventservice.database.entities.enums.EventStatus
import ru.underfish.eventservice.dto.filter.EventSearchFilter
import java.math.BigDecimal

object EventSpecifications {
    fun build(filter: EventSearchFilter): Specification<Event> {
        var spec = Specification.allOf<Event>()

        filter.title?.takeIf { it.isNotBlank() }?.let { value ->
            spec = spec.and { root, _, cb -> cb.like(cb.lower(root.get("title")), "%${value.lowercase()}%") }
        }

        filter.status?.let { raw ->
            val status = parseStatus(raw)
            spec = spec.and { root, _, cb -> cb.equal(root.get<EventStatus>("eventStatus"), status) }
        }

        filter.isOnline?.let { online ->
            spec = spec.and { root, _, cb -> cb.equal(root.get<Boolean>("isOnline"), online) }
        }

        filter.startFrom?.let { from ->
            spec = spec.and { root, _, cb -> cb.greaterThanOrEqualTo(root.get("startDatetime"), from) }
        }

        filter.startTo?.let { to ->
            spec = spec.and { root, _, cb -> cb.lessThanOrEqualTo(root.get("startDatetime"), to) }
        }

        filter.priceMin?.let { min ->
            spec = spec.and { root, _, cb -> cb.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(min)) }
        }

        filter.priceMax?.let { max ->
            spec = spec.and { root, _, cb -> cb.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(max)) }
        }

        filter.locationId?.let { locationId ->
            spec = spec.and { root, _, cb -> cb.equal(root.get<Any>("locationId"), locationId) }
        }

        filter.communityId?.let { communityId ->
            spec = spec.and { root, _, cb -> cb.equal(root.get<Any>("communityId"), communityId) }
        }

        filter.organizerId?.let { organizerId ->
            spec = spec.and { root, _, cb -> cb.equal(root.get<Any>("organizerId"), organizerId) }
        }

        if (filter.tagIds.isNotEmpty()) {
            spec =
                spec.and { root, query, cb ->
                    query.distinct(true)
                    val tagsJoin = root.join<Event, Tag>("tags", JoinType.LEFT)
                    tagsJoin.get<Any>("id").`in`(filter.tagIds)
                }
        }

        return spec
    }

    private fun parseStatus(raw: String): EventStatus =
        EventStatus.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            ?: throw IllegalArgumentException("Invalid event status")
}
