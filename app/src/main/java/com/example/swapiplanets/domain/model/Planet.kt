package com.example.swapiplanets.domain.model

data class Planet(
    val id: String,
    val name: String,
    val climate: String,
    val terrain: String,
    val population: String,
    val rotationPeriod: String?,
    val orbitalPeriod: String?,
    val diameter: String?
)