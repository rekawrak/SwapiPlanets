package com.example.swapiplanets.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_planets")
data class CachedPlanetEntity(
    @PrimaryKey val planetId: String,
    val name: String,
    val climate: String,
    val terrain: String,
    val population: String,
    val rotationPeriod: String?,
    val orbitalPeriod: String?,
    val diameter: String?,
    val cachedAtMs: Long
)
