package com.example.swapiplanets.domain.model

data class UserProfile(
    val profileId: String,
    val displayName: String,
    val createdAtMs: Long
)
