package com.example.swapiplanets.di

import com.example.swapiplanets.data.repository.PlanetRepositoryImpl
import com.example.swapiplanets.data.repository.RoomFavouritesRepository
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlanetRepository(
        impl: PlanetRepositoryImpl
    ): PlanetRepository

    @Binds
    @Singleton
    abstract fun bindFavouritesRepository(
        impl: RoomFavouritesRepository
    ): FavouritesRepository
}