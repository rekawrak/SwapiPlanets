package com.example.swapiplanets.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlanetCacheDao {
    @Query("SELECT * FROM cached_planets ORDER BY name ASC")
    suspend fun getAll(): List<CachedPlanetEntity>

    @Query("SELECT * FROM cached_planets WHERE planetId = :id LIMIT 1")
    suspend fun getById(id: String): CachedPlanetEntity?

    @Query("SELECT * FROM cached_planets WHERE planetId IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<CachedPlanetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedPlanetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<CachedPlanetEntity>)
}
