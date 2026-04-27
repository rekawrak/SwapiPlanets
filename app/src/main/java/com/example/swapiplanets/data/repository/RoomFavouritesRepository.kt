package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.FavouritePlanetEntity
import com.example.swapiplanets.data.local.FavouritesDao
import com.example.swapiplanets.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomFavouritesRepository @Inject constructor(
    private val dao: FavouritesDao
) : FavouritesRepository {

    override fun observeAll(): Flow<Set<String>> {
        return dao.observeAll().map { entities -> entities.map { it.planetId }.toSet() }
    }

    override suspend fun getAll(): Set<String> {
        return dao.getAll().map { it.planetId }.toSet()
    }

    override suspend fun isFavourite(id: String): Boolean {
        return dao.isFavourite(id)
    }

    override suspend fun toggle(id: String) {
        if (dao.isFavourite(id)) {
            dao.delete(FavouritePlanetEntity(planetId = id))
        } else {
            dao.insert(FavouritePlanetEntity(planetId = id))
        }
    }
}
