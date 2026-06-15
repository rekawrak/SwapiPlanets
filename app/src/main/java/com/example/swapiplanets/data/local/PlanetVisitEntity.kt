package com.example.swapiplanets.data.local

import androidx.room.Entity

@Entity(
    tableName = "planet_visits",
    primaryKeys = ["profileId", "planetId"]
)
data class PlanetVisitEntity(
    val profileId: String,
    val planetId: String,
    val planetName: String,
    val visitedAtMs: Long
)
