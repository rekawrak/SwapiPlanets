package com.example.swapiplanets.di

import android.content.Context
import androidx.room.Room
import com.example.swapiplanets.data.local.AppDatabase
import com.example.swapiplanets.data.local.FavouritesDao
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavouritesDao(database: AppDatabase): FavouritesDao = database.favouritesDao()
}
