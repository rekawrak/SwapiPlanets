package com.example.swapiplanets.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.model.PlanetCollection
import com.example.swapiplanets.ui.common.UiState
import com.example.swapiplanets.ui.components.ErrorView
import com.example.swapiplanets.ui.components.LoadingView
import com.example.swapiplanets.ui.components.StarRatingRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetDetailScreen(
    state: UiState<Planet>,
    isFavourite: Boolean,
    noteText: String,
    isOfflineContent: Boolean,
    isPinned: Boolean,
    rating: Int?,
    pinMessage: String?,
    collections: List<PlanetCollection>,
    planetCollectionIds: Set<String>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavourite: () -> Unit,
    onTogglePin: () -> Unit,
    onRatingSelected: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveNote: () -> Unit,
    onToggleCollection: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onToggleFavourite) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                title = {
                    val title = when (state) {
                        is UiState.Content -> state.data.name
                        else -> "Planet"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { innerPadding ->
        when (state) {
            UiState.Loading -> LoadingView(modifier = Modifier.padding(innerPadding))
            is UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    verticalArrangement = Arrangement.Center
                ) {
                    ErrorView(message = state.message, onRetry = onRetry)
                }
            }
            UiState.Empty -> LoadingView(modifier = Modifier.padding(innerPadding))
            is UiState.Content -> {
                val planet = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isOfflineContent) {
                        Text(
                            text = "Showing cached data",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    pinMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = planet.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            DetailField("Climate", planet.climate)
                            DetailField("Terrain", planet.terrain)
                            DetailField("Population", planet.population)
                            planet.diameter?.let {
                                Text(text = "Diameter: $it", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Your rating", style = MaterialTheme.typography.titleMedium)
                            StarRatingRow(rating = rating, onRatingSelected = onRatingSelected)
                        }
                    }

                    if (collections.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Collections", style = MaterialTheme.typography.titleMedium)
                                collections.forEach { collection ->
                                    FilterChip(
                                        selected = planetCollectionIds.contains(collection.collectionId),
                                        onClick = { onToggleCollection(collection.collectionId) },
                                        label = { Text(collection.name) }
                                    )
                                }
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Personal note", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = onNoteChange,
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                label = { Text("Your note about this planet") }
                            )
                            Button(onClick = onSaveNote) {
                                Text("Save note")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
    Text(text = value, style = MaterialTheme.typography.bodyLarge)
}
