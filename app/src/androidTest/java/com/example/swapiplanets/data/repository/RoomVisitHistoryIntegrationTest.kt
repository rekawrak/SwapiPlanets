package com.example.swapiplanets.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.swapiplanets.data.local.AppDatabase
import com.example.swapiplanets.data.repository.RoomUserProfileRepository
import com.example.swapiplanets.data.repository.RoomVisitHistoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomVisitHistoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var profileRepository: RoomUserProfileRepository
    private lateinit var repository: RoomVisitHistoryRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        profileRepository = RoomUserProfileRepository(
            database,
            database.userProfileDao(),
            PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile("test_visits.pb") }
            )
        )
        runBlocking { profileRepository.ensureDefaultProfile() }
        repository = RoomVisitHistoryRepository(database.visitHistoryDao(), profileRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordVisit_appearsInRecentList() = runBlocking {
        repository.recordVisit("1", "Tatooine")
        repository.recordVisit("2", "Alderaan")

        repository.observeRecent(limit = 10).test {
            val visits = awaitItem()
            assertEquals(2, visits.size)
            assertEquals("Alderaan", visits.first().planetName)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
