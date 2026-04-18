package ru.underfish.locations_service.service

import org.springframework.stereotype.Service
import ru.underfish.locations_service.database.dao.LocationRepository
import ru.underfish.locations_service.database.entities.Location
import ru.underfish.locations_service.dto.request.LocationRequest
import ru.underfish.locations_service.dto.response.LocationResponse
import ru.underfish.locations_service.exeption.NotFoundException

@Service
class LocationService(
    private val locationRepository: LocationRepository
) {

    fun createLocation(request: LocationRequest): LocationResponse {
        val location = Location(
            latitude = request.latitude,
            longitude = request.longitude,
            address = request.address,
            city = request.city,
            district = request.district,
            placeName = request.placeName,
            timezone = request.timezone
        )

        val savedLocation = locationRepository.save(location)
        return LocationResponse.fromEntity(savedLocation)
    }

    fun getLocation(locationId: Long): LocationResponse {
        val location = locationRepository.findLocationById(locationId) ?: throw NotFoundException("Location not found")
        return LocationResponse.fromEntity(location)
    }

    fun updateLocation(locationId: Long, request: LocationRequest): LocationResponse {
        val location = locationRepository.findLocationById(locationId) ?: throw NotFoundException("Location not found")

        location.latitude = request.latitude
        location.longitude = request.longitude
        location.address = request.address
        location.city = request.city
        location.district = request.district
        location.placeName = request.placeName
        location.timezone = request.timezone

        val updatedLocation = locationRepository.save(location)
        return LocationResponse.fromEntity(updatedLocation)
    }

    fun deleteLocation(locationId: Long) {
        val location = locationRepository.findLocationById(locationId) ?: throw NotFoundException("Location not found")

        locationRepository.delete(location)
    }
}