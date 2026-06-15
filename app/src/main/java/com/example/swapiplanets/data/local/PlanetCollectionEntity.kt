package com.example.swapiplanets.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planet_collections")
data class PlanetCollectionEntity(
    @PrimaryKey val collectionId: String,
    val profileId: String,
    val name: String,
    val createdAtMs: Long
)
