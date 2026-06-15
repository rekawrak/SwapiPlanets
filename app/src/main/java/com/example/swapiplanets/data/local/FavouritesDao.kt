package com.example.swapiplanets.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDao {
    @Query("SELECT * FROM favourite_planets WHERE profileId = :profileId")
    suspend fun getAll(profileId: String): List<FavouritePlanetEntity>

    @Query("SELECT * FROM favourite_planets WHERE profileId = :profileId")
    fun observeAll(profileId: String): Flow<List<FavouritePlanetEntity>>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favourite_planets WHERE profileId = :profileId AND planetId = :planetId)"
    )
    suspend fun isFavourite(profileId: String, planetId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavouritePlanetEntity): Long

    @Delete
    suspend fun delete(entity: FavouritePlanetEntity)

    @Query(
        "UPDATE favourite_planets SET lastSyncedAtMs = :syncedAtMs " +
            "WHERE profileId = :profileId AND planetId = :planetId"
    )
    suspend fun updateLastSynced(profileId: String, planetId: String, syncedAtMs: Long)

    @Query("DELETE FROM favourite_planets WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)
}
