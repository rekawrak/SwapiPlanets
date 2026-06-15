package com.example.swapiplanets.data.mapper

import com.example.swapiplanets.data.local.CachedPlanetEntity
import com.example.swapiplanets.data.local.PlanetNoteEntity
import com.example.swapiplanets.data.local.PlanetVisitEntity
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.model.PlanetNote
import com.example.swapiplanets.domain.model.RecentVisit

fun Planet.toCachedEntity(cachedAtMs: Long): CachedPlanetEntity = CachedPlanetEntity(
    planetId = id,
    name = name,
    climate = climate,
    terrain = terrain,
    population = population,
    rotationPeriod = rotationPeriod,
    orbitalPeriod = orbitalPeriod,
    diameter = diameter,
    cachedAtMs = cachedAtMs
)

fun CachedPlanetEntity.toDomain(): Planet = Planet(
    id = planetId,
    name = name,
    climate = climate,
    terrain = terrain,
    population = population,
    rotationPeriod = rotationPeriod,
    orbitalPeriod = orbitalPeriod,
    diameter = diameter
)

fun PlanetVisitEntity.toDomain(): RecentVisit = RecentVisit(
    planetId = planetId,
    planetName = planetName,
    visitedAtMs = visitedAtMs
)

fun PlanetNoteEntity.toDomain(): PlanetNote = PlanetNote(
    planetId = planetId,
    planetName = planetName,
    noteText = noteText,
    updatedAtMs = updatedAtMs
)
