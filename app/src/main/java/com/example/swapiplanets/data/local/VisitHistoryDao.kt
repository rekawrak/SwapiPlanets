package com.example.swapiplanets.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlanetVisitEntity)

    @Query(
        "SELECT * FROM planet_visits WHERE profileId = :profileId " +
            "ORDER BY visitedAtMs DESC LIMIT :limit"
    )
    fun observeRecent(profileId: String, limit: Int): Flow<List<PlanetVisitEntity>>

    @Query(
        "SELECT * FROM planet_visits WHERE profileId = :profileId " +
            "ORDER BY visitedAtMs DESC LIMIT :limit"
    )
    suspend fun getRecent(profileId: String, limit: Int): List<PlanetVisitEntity>

    @Query("DELETE FROM planet_visits WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)
}
