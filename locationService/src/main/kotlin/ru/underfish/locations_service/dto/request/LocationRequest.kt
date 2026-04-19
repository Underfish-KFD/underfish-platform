package ru.underfish.locations_service.dto.request

import java.math.BigDecimal

data class LocationRequest(
    val address: String?,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val city: String?,
    val district: String?,
    val placeName: String?,
    val timezone: String
)