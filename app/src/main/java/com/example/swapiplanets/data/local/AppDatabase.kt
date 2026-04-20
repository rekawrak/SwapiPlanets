package com.example.swapiplanets.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavouritePlanetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouritesDao(): FavouritesDao
}
