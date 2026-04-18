package ru.underfish.locations_service.contoller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.underfish.locations_service.dto.request.LocationRequest
import ru.underfish.locations_service.dto.response.LocationResponse
import ru.underfish.locations_service.service.LocationService

@RestController
@RequestMapping("/api/v1/locations")
class LocationController(private val locationService: LocationService) {

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    fun createLocation(@RequestBody request: LocationRequest): LocationResponse {
        return locationService.createLocation(request)
    }

    @GetMapping("/{locationId}")
    fun getLocation(@PathVariable locationId: Long): LocationResponse {
        return locationService.getLocation(locationId)
    }

    @PutMapping("/{locationId}")
    fun updateLocation(
        @PathVariable locationId: Long,
        @RequestBody request: LocationRequest
    ): LocationResponse {
        return locationService.updateLocation(locationId, request)
    }

    @DeleteMapping("/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLocation(@PathVariable locationId: Long) {
        locationService.deleteLocation(locationId)
    }
}
