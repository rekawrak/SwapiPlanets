package com.example.swapiplanets.data.local

import androidx.room.Entity

@Entity(
    tableName = "planet_user_states",
    primaryKeys = ["profileId", "planetId"]
)
data class PlanetUserStateEntity(
    val profileId: String,
    val planetId: String,
    val isRead: Boolean = false,
    val isPinned: Boolean = false,
    val pinnedOrder: Int? = null,
    val rating: Int? = null,
    val readAtMs: Long? = null
)
