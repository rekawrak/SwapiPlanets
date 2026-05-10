package com.example.swapiplanets.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_planets")
data class FavouritePlanetEntity(
    @PrimaryKey val planetId: String
)
