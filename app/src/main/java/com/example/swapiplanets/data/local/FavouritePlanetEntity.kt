package com.example.swapiplanets.data.local

import androidx.room.Entity

@Entity(
    tableName = "favourite_planets",
    primaryKeys = ["profileId", "planetId"]
)
data class FavouritePlanetEntity(
    val profileId: String,
    val planetId: String,
    val addedAtMs: Long,
    val lastSyncedAtMs: Long?
)
