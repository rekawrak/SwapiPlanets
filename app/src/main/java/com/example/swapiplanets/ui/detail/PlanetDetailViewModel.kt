package com.example.swapiplanets.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.model.PlanetCollection
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.FavouritesSyncRepository
import com.example.swapiplanets.domain.repository.PlanetCollectionRepository
import com.example.swapiplanets.domain.repository.PlanetNotesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.domain.repository.PlanetUserStateRepository
import com.example.swapiplanets.domain.repository.PinResult
import com.example.swapiplanets.domain.repository.VisitHistoryRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

private const val ARG_PLANET_ID = "planetId"

@HiltViewModel
class PlanetDetailViewModel @Inject constructor(
    private val repository: PlanetRepository,
    private val favouritesRepository: FavouritesRepository,
    private val visitHistoryRepository: VisitHistoryRepository,
    private val notesRepository: PlanetNotesRepository,
    private val favouritesSyncRepository: FavouritesSyncRepository,
    private val userStateRepository: PlanetUserStateRepository,
    private val collectionRepository: PlanetCollectionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val planetId: String = checkNotNull(savedStateHandle[ARG_PLANET_ID])

    var state: UiState<Planet> by mutableStateOf(UiState.Loading)
        private set

    var isFavourite: Boolean by mutableStateOf(false)
        private set

    var noteText: String by mutableStateOf("")
        private set

    var isOfflineContent: Boolean by mutableStateOf(false)
        private set

    var isPinned: Boolean by mutableStateOf(false)
        private set

    var rating: Int? by mutableStateOf(null)
        private set

    private val _pinMessage = MutableStateFlow<String?>(null)
    val pinMessage: StateFlow<String?> = _pinMessage.asStateFlow()

    val collections: StateFlow<List<PlanetCollection>> = collectionRepository.observeCollections()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val planetCollectionIds: StateFlow<Set<String>> =
        collectionRepository.observePlanetCollectionIds(planetId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun loadPlanet() {
        state = UiState.Loading
        viewModelScope.launch {
            val cached = repository.getCachedPlanetDetail(planetId)
            if (cached != null) {
                state = UiState.Content(cached)
                isOfflineContent = true
            }
            try {
                val planet = repository.getPlanetDetail(planetId)
                state = UiState.Content(planet)
                isOfflineContent = false
                visitHistoryRepository.recordVisit(planetId, planet.name)
                userStateRepository.markAsRead(planetId)
            } catch (e: Exception) {
                if (cached == null) {
                    state = UiState.Error(toReadableError(e))
                } else {
                    userStateRepository.markAsRead(planetId)
                }
            }
        }
    }

    fun onNoteChange(text: String) {
        noteText = text
    }

    fun saveNote() {
        val planetName = (state as? UiState.Content)?.data?.name ?: return
        viewModelScope.launch {
            notesRepository.saveNote(planetId, planetName, noteText)
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            favouritesRepository.toggle(planetId)
            favouritesSyncRepository.scheduleImmediateSync()
        }
    }

    fun togglePin() {
        viewModelScope.launch {
            when (userStateRepository.togglePin(planetId)) {
                PinResult.Pinned -> _pinMessage.value = null
                PinResult.Unpinned -> _pinMessage.value = null
                PinResult.LimitReached -> _pinMessage.value = "Maximum 5 pinned planets."
            }
        }
    }

    fun setRating(value: Int) {
        viewModelScope.launch {
            userStateRepository.setRating(planetId, value)
        }
    }

    fun toggleCollectionMembership(collectionId: String) {
        val planetName = (state as? UiState.Content)?.data?.name ?: return
        viewModelScope.launch {
            collectionRepository.togglePlanetInCollection(collectionId, planetId, planetName)
        }
    }

    private fun observeFavouriteState() {
        viewModelScope.launch {
            favouritesRepository.observeAll().collectLatest { ids ->
                isFavourite = ids.contains(planetId)
            }
        }
    }

    private fun observeNote() {
        viewModelScope.launch {
            notesRepository.observeNote(planetId).collectLatest { text ->
                noteText = text.orEmpty()
            }
        }
    }

    private fun observeUserState() {
        viewModelScope.launch {
            userStateRepository.observeAllStates().collectLatest { states ->
                val planetState = states[planetId]
                isPinned = planetState?.isPinned == true
                rating = planetState?.rating
            }
        }
    }

    private fun toReadableError(error: Throwable): String {
        return when (error) {
            is IOException -> "No internet connection. Check your network and retry."
            is HttpException -> "Server error (${error.code()}). Please try again."
            else -> "Could not load planet details. Please try again."
        }
    }

    init {
        observeFavouriteState()
        observeNote()
        observeUserState()
        loadPlanet()
    }
}