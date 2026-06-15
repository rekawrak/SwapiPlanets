package com.example.swapiplanets.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.PlanetCollection
import com.example.swapiplanets.domain.repository.PlanetCollectionRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: PlanetCollectionRepository
) : ViewModel() {

    val state: StateFlow<UiState<List<PlanetCollection>>> =
        collectionRepository.observeCollections()
            .map { collections ->
                if (collections.isEmpty()) UiState.Empty else UiState.Content(collections)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    fun createCollection(name: String) {
        viewModelScope.launch {
            runCatching { collectionRepository.createCollection(name) }
        }
    }

    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId)
        }
    }
}
