package ru.underfish.locations_service.database.dao

import ru.underfish.locations_service.database.entities.Location

interface LocationRepository : AbstractRepository<Location> {
    fun findLocationById(id: Long): Location?
}