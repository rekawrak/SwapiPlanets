package com.example.swapiplanets.di

import android.content.Context
import androidx.room.Room
import com.example.swapiplanets.data.local.AppDatabase
import com.example.swapiplanets.data.local.FavouritesDao
import com.example.swapiplanets.data.local.PlanetCacheDao
import com.example.swapiplanets.data.local.PlanetCollectionDao
import com.example.swapiplanets.data.local.PlanetNotesDao
import com.example.swapiplanets.data.local.PlanetUserStateDao
import com.example.swapiplanets.data.local.UserProfileDao
import com.example.swapiplanets.data.local.VisitHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "swapi_planets.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    @Singleton
    fun provideFavouritesDao(database: AppDatabase): FavouritesDao = database.favouritesDao()

    @Provides
    @Singleton
    fun providePlanetCacheDao(database: AppDatabase): PlanetCacheDao = database.planetCacheDao()

    @Provides
    @Singleton
    fun provideVisitHistoryDao(database: AppDatabase): VisitHistoryDao = database.visitHistoryDao()

    @Provides
    @Singleton
    fun providePlanetNotesDao(database: AppDatabase): PlanetNotesDao = database.planetNotesDao()

    @Provides
    @Singleton
    fun providePlanetCollectionDao(database: AppDatabase): PlanetCollectionDao =
        database.planetCollectionDao()

    @Provides
    @Singleton
    fun providePlanetUserStateDao(database: AppDatabase): PlanetUserStateDao =
        database.planetUserStateDao()
}
