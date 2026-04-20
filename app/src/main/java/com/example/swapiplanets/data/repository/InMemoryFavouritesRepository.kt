package com.example.swapiplanets.data.repository

import com.example.swapiplanets.domain.repository.FavouritesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryFavouritesRepository @Inject constructor() : FavouritesRepository {

    private val favourites = mutableSetOf<String>()

    override suspend fun getAll(): Set<String> = favourites.toSet()

    override suspend fun isFavourite(id: String): Boolean = favourites.contains(id)

    override suspend fun toggle(id: String): Set<String> {
        if (!favourites.add(id)) {
            favourites.remove(id)
        }
        return favourites.toSet()
    }
}
