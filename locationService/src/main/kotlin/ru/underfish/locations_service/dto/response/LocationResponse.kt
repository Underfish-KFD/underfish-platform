package ru.underfish.locations_service.dto.response

import ru.underfish.locations_service.database.entities.Location
import java.time.LocalDateTime
import java.math.BigDecimal

data class LocationResponse(
    val locationId: String,
    val address: String?,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val city: String?,
    val district: String?,
    val placeName: String?,
    val timezone: String
) {
    companion object {
        fun fromEntity(location: Location): LocationResponse {
            return LocationResponse(
                locationId = location.id.toString(),
                address = location.address ?: "",
                latitude = location.latitude,
                longitude = location.longitude,
                city = location.city ?: "",
                district = location.district ?: "",
                placeName = location.placeName ?: "",
                timezone = location.timezone
            )
        }
    }
}
