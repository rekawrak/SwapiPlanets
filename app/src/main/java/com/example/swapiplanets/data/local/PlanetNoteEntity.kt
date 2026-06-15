package com.example.swapiplanets.data.local

import androidx.room.Entity

@Entity(
    tableName = "planet_notes",
    primaryKeys = ["profileId", "planetId"]
)
data class PlanetNoteEntity(
    val profileId: String,
    val planetId: String,
    val planetName: String,
    val noteText: String,
    val updatedAtMs: Long
)
