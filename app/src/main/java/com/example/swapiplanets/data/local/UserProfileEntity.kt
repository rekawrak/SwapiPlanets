package com.example.swapiplanets.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val profileId: String,
    val displayName: String,
    val createdAtMs: Long
)
