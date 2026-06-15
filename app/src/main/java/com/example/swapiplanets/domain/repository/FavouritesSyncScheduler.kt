package com.example.swapiplanets.domain.repository

interface FavouritesSyncScheduler {
    fun schedulePeriodicSync()
    fun scheduleOneTimeSync()
    fun cancelPeriodicSync()
}
