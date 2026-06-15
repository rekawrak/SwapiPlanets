package com.example.swapiplanets.di

import com.example.swapiplanets.data.repository.OfflineFirstPlanetRepository
import com.example.swapiplanets.data.repository.RemotePlanetDataSource
import com.example.swapiplanets.data.repository.RoomFavouritesRepository
import com.example.swapiplanets.data.repository.RoomPlanetCacheRepository
import com.example.swapiplanets.data.repository.RoomPlanetNotesRepository
import com.example.swapiplanets.data.repository.RoomVisitHistoryRepository
import com.example.swapiplanets.data.repository.RoomUserProfileRepository
import com.example.swapiplanets.data.repository.RoomPlanetCollectionRepository
import com.example.swapiplanets.data.repository.RoomPlanetUserStateRepository
import com.example.swapiplanets.data.sync.FavouritesSyncRepositoryImpl
import com.example.swapiplanets.data.sync.FavouritesSyncWorkScheduler
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.FavouritesSyncRepository
import com.example.swapiplanets.domain.repository.FavouritesSyncScheduler
import com.example.swapiplanets.domain.repository.PlanetCacheRepository
import com.example.swapiplanets.domain.repository.PlanetNotesRepository
import com.example.swapiplanets.domain.repository.PlanetRemoteDataSource
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
import com.example.swapiplanets.domain.repository.VisitHistoryRepository
import com.example.swapiplanets.domain.repository.PlanetCollectionRepository
import com.example.swapiplanets.domain.repository.PlanetUserStateRepository
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
        impl: OfflineFirstPlanetRepository
    ): PlanetRepository

    @Binds
    @Singleton
    abstract fun bindFavouritesRepository(
        impl: RoomFavouritesRepository
    ): FavouritesRepository

    @Binds
    @Singleton
    abstract fun bindPlanetCacheRepository(
        impl: RoomPlanetCacheRepository
    ): PlanetCacheRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: RoomUserProfileRepository
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindVisitHistoryRepository(
        impl: RoomVisitHistoryRepository
    ): VisitHistoryRepository

    @Binds
    @Singleton
    abstract fun bindPlanetNotesRepository(
        impl: RoomPlanetNotesRepository
    ): PlanetNotesRepository

    @Binds
    @Singleton
    abstract fun bindPlanetCollectionRepository(
        impl: RoomPlanetCollectionRepository
    ): PlanetCollectionRepository

    @Binds
    @Singleton
    abstract fun bindPlanetUserStateRepository(
        impl: RoomPlanetUserStateRepository
    ): PlanetUserStateRepository

    @Binds
    @Singleton
    abstract fun bindFavouritesSyncRepository(
        impl: FavouritesSyncRepositoryImpl
    ): FavouritesSyncRepository

    @Binds
    @Singleton
    abstract fun bindPlanetRemoteDataSource(
        impl: RemotePlanetDataSource
    ): PlanetRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindFavouritesSyncScheduler(
        impl: FavouritesSyncWorkScheduler
    ): FavouritesSyncScheduler
}