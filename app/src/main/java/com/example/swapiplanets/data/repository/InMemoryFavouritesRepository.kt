package com.example.swapiplanets.data.repository

import com.example.swapiplanets.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryFavouritesRepository @Inject constructor() : FavouritesRepository {

    private val favourites = mutableSetOf<String>()
    private val favouritesFlow = MutableStateFlow<Set<String>>(emptySet())

    override fun observeAll(): Flow<Set<String>> = favouritesFlow.asStateFlow()

    override suspend fun getAll(): Set<String> = favourites.toSet()

    override suspend fun isFavourite(id: String): Boolean = favourites.contains(id)

    override suspend fun toggle(id: String) {
        if (!favourites.add(id)) {
            favourites.remove(id)
        }
        favouritesFlow.value = favourites.toSet()
    }
}
