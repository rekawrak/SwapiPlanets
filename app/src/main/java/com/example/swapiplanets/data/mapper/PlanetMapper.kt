package com.example.swapiplanets.data.mapper

import com.example.swapiplanets.data.dto.PlanetDto
import com.example.swapiplanets.domain.model.Planet

fun PlanetDto.toDomain(): Planet {
    val id = url.trimEnd('/').substringAfterLast('/')
    return Planet(
        id = id,
        name = name,
        climate = climate,
        terrain = terrain,
        population = population,
        rotationPeriod = rotationPeriod,
        orbitalPeriod = orbitalPeriod,
        diameter = diameter
    )
}