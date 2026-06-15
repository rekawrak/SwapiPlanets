package com.example.swapiplanets

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.swapiplanets.domain.repository.FavouritesSyncRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class SwapiPlanetsApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var favouritesSyncRepository: FavouritesSyncRepository

    @Inject
    lateinit var userProfileRepository: UserProfileRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            userProfileRepository.ensureDefaultProfile()
        }
        favouritesSyncRepository.scheduleBackgroundSync()
    }
}
