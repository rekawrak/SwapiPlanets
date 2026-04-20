package com.example.swapiplanets.domain.repository

interface FavouritesRepository {
    suspend fun getAll(): Set<String>
    suspend fun isFavourite(id: String): Boolean
    suspend fun toggle(id: String): Set<String>
}
