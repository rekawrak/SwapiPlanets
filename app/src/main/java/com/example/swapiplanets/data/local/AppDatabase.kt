package com.example.swapiplanets.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        FavouritePlanetEntity::class,
        CachedPlanetEntity::class,
        PlanetVisitEntity::class,
        PlanetNoteEntity::class,
        PlanetCollectionEntity::class,
        PlanetCollectionItemEntity::class,
        PlanetUserStateEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun favouritesDao(): FavouritesDao
    abstract fun planetCacheDao(): PlanetCacheDao
    abstract fun visitHistoryDao(): VisitHistoryDao
    abstract fun planetNotesDao(): PlanetNotesDao
    abstract fun planetCollectionDao(): PlanetCollectionDao
    abstract fun planetUserStateDao(): PlanetUserStateDao
}
