package com.example.swapiplanets.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDao {
    @Query("SELECT * FROM favourite_planets")
    suspend fun getAll(): List<FavouritePlanetEntity>

    @Query("SELECT * FROM favourite_planets")
    fun observeAll(): Flow<List<FavouritePlanetEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_planets WHERE planetId = :id)")
    suspend fun isFavourite(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavouritePlanetEntity): Long

    @Delete
    suspend fun delete(entity: FavouritePlanetEntity)
}
