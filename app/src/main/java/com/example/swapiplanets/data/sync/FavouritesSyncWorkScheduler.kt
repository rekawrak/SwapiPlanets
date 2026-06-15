package com.example.swapiplanets.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.swapiplanets.domain.repository.FavouritesSyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesSyncWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : FavouritesSyncScheduler {
    private val workManager = WorkManager.getInstance(context)

    override fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<FavouritesSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun scheduleOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<FavouritesSyncWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    companion object {
        const val PERIODIC_WORK_NAME = "favourites_periodic_sync"
        const val ONE_TIME_WORK_NAME = "favourites_immediate_sync"
    }
}