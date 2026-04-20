package com.example.swapiplanets.data.dto

import com.google.gson.annotations.SerializedName

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
    @SerializedName("rotation_period") val rotationPeriod: String?,
    @SerializedName("orbital_period") val orbitalPeriod: String?,
    val diameter: String?,
    val url: String
)