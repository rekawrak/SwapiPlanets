package com.example.swapiplanets.ui.collections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.PlanetCollectionItem
import com.example.swapiplanets.domain.repository.PlanetCollectionRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ARG_COLLECTION_ID = "collectionId"

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val collectionRepository: PlanetCollectionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val collectionId: String = checkNotNull(savedStateHandle[ARG_COLLECTION_ID])

    val state: StateFlow<UiState<List<PlanetCollectionItem>>> =
        collectionRepository.observeCollectionItems(collectionId)
            .map { items ->
                if (items.isEmpty()) UiState.Empty else UiState.Content(items)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    fun removePlanet(planetId: String) {
        viewModelScope.launch {
            collectionRepository.removePlanetFromCollection(collectionId, planetId)
        }
    }
}
