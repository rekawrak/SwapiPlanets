package com.example.swapiplanets.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryFavouritesRepositoryFlowTest {

    @Test
    fun observeAll_emitsFullSequence_whenToggled() = runTest {
        val repository = InMemoryFavouritesRepository()

        repository.observeAll().test {
            assertEquals(emptySet<String>(), awaitItem())

            repository.toggle("1")
            assertEquals(setOf("1"), awaitItem())

            repository.toggle("1")
            assertEquals(emptySet<String>(), awaitItem())
        }
    }

    @Test
    fun observeAll_newSubscriberReceivesLatestValue() = runTest {
        val repository = InMemoryFavouritesRepository()
        repository.toggle("99")

        repository.observeAll().test {
            assertEquals(setOf("99"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
