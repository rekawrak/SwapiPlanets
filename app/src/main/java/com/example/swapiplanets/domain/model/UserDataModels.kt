package com.example.swapiplanets.domain.model

data class RecentVisit(
    val planetId: String,
    val planetName: String,
    val visitedAtMs: Long
)

data class PlanetNote(
    val planetId: String,
    val planetName: String,
    val noteText: String,
    val updatedAtMs: Long
)

data class FavouriteWithCacheStatus(
    val planet: Planet,
    val isStale: Boolean,
    val lastSyncedAtMs: Long?
)
