package com.example.swapiplanets.data.dto

import com.squareup.moshi.Json

data class PlanetListResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PlanetDto>
)

data class PlanetDto(
    val name: String,
    val climate: String,
    val terrain: String,
    val population: String,
    @Json(name = "rotation_period") val rotationPeriod: String?,
    @Json(name = "orbital_period") val orbitalPeriod: String?,
    val diameter: String?,
    val url: String
)