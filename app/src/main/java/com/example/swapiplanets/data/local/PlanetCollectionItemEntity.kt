package com.example.swapiplanets.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "planet_collection_items",
    primaryKeys = ["collectionId", "planetId"],
    foreignKeys = [
        ForeignKey(
            entity = PlanetCollectionEntity::class,
            parentColumns = ["collectionId"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId"), Index("profileId")]
)
data class PlanetCollectionItemEntity(
    val collectionId: String,
    val profileId: String,
    val planetId: String,
    val planetName: String,
    val addedAtMs: Long
)
