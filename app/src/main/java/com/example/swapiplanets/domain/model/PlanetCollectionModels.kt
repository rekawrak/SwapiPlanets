package com.example.swapiplanets.domain.model

data class PlanetCollection(
    val collectionId: String,
    val name: String,
    val createdAtMs: Long,
    val planetCount: Int = 0
)

data class PlanetCollectionItem(
    val collectionId: String,
    val planetId: String,
    val planetName: String,
    val addedAtMs: Long
)

data class PlanetUserState(
    val planetId: String,
    val isRead: Boolean,
    val isPinned: Boolean,
    val pinnedOrder: Int?,
    val rating: Int?
)
