package com.example.swapiplanets.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles ORDER BY createdAtMs ASC")
    fun observeAll(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles ORDER BY createdAtMs ASC")
    suspend fun getAll(): List<UserProfileEntity>

    @Query("SELECT * FROM user_profiles WHERE profileId = :profileId LIMIT 1")
    suspend fun getById(profileId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE profileId = :profileId")
    suspend fun delete(profileId: String)

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun count(): Int
}
