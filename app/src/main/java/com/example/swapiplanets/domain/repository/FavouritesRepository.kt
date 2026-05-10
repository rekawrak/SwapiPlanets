package com.example.swapiplanets.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavouritesRepository {
    fun observeAll(): Flow<Set<String>>
    suspend fun getAll(): Set<String>
    suspend fun isFavourite(id: String): Boolean
    suspend fun toggle(id: String)
}
