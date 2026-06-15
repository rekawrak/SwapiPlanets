package com.example.swapiplanets.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.model.PlanetUserState
import com.example.swapiplanets.ui.common.UiState
import com.example.swapiplanets.ui.components.EmptyView
import com.example.swapiplanets.ui.components.ErrorView
import com.example.swapiplanets.ui.components.LoadingView
import com.example.swapiplanets.ui.components.StarRatingRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetListScreen(
    state: UiState<List<Planet>>,
    query: String,
    favouriteIds: Set<String>,
    onlyFavourites: Boolean,
    sortNamesDescending: Boolean,
    isOfflineData: Boolean,
    planetIdsWithNotes: Set<String>,
    userStates: Map<String, PlanetUserState>,
    pinnedPlanetIds: List<String>,
    pinFeedback: String?,
    onQueryChange: (String) -> Unit,
    onOnlyFavouritesChange: (Boolean) -> Unit,
    onSortOrderChange: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onFavouritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRecentClick: () -> Unit,
    onNotesClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onPlanetClick: (String) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onTogglePin: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onCollectionsClick) {
                        Icon(Icons.Filled.Bookmark, contentDescription = "Collections", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onRecentClick) {
                        Icon(Icons.Filled.List, contentDescription = "Recent", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onNotesClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Notes", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onFavouritesClick) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favourites", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                title = {
                    Text(
                        text = "SWAPI Planets",
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
            UiState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ListSearchAndFilters(
                        query, onlyFavourites, sortNamesDescending, isOfflineData, pinFeedback,
                        onQueryChange, onOnlyFavouritesChange, onSortOrderChange
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    EmptyView(message = "Try another search query.")
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            is UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ErrorView(message = state.message, onRetry = onRetry)
                }
            }
            is UiState.Content -> {
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    ListSearchAndFilters(
                        query, onlyFavourites, sortNamesDescending, isOfflineData, pinFeedback,
                        onQueryChange, onOnlyFavouritesChange, onSortOrderChange
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data, key = { it.id }) { planet ->
                            val userState = userStates[planet.id]
                            PlanetListItem(
                                planet = planet,
                                isFavourite = favouriteIds.contains(planet.id),
                                hasNote = planetIdsWithNotes.contains(planet.id),
                                isRead = userState?.isRead == true,
                                isPinned = pinnedPlanetIds.contains(planet.id),
                                rating = userState?.rating,
                                onClick = { onPlanetClick(planet.id) },
                                onToggleFavourite = { onToggleFavourite(planet.id) },
                                onTogglePin = { onTogglePin(planet.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListSearchAndFilters(
    query: String,
    onlyFavourites: Boolean,
    sortNamesDescending: Boolean,
    isOfflineData: Boolean,
    pinFeedback: String?,
    onQueryChange: (String) -> Unit,
    onOnlyFavouritesChange: (Boolean) -> Unit,
    onSortOrderChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (isOfflineData) {
            Text(
                text = "Offline mode — showing cached planets",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        pinFeedback?.let {
            Text(text = it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(4.dp))
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search by planet name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterChip(
                selected = onlyFavourites,
                onClick = { onOnlyFavouritesChange(!onlyFavourites) },
                label = { Text("Favourites only") }
            )
            TextButton(onClick = { onSortOrderChange(!sortNamesDescending) }) {
                Text(if (sortNamesDescending) "Sort: Z → A" else "Sort: A → Z")
            }
        }
    }
}

@Composable
private fun PlanetListItem(
    planet: Planet,
    isFavourite: Boolean,
    hasNote: Boolean,
    isRead: Boolean,
    isPinned: Boolean,
    rating: Int?,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit,
    onTogglePin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPinned) 4.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRead) "✓" else "○",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(24.dp)
                )
                Text(
                    text = planet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isPinned) {
                    Text(
                        text = "Pinned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (hasNote) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            rating?.let {
                StarRatingRow(rating = it, onRatingSelected = {}, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Climate: ${planet.climate}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Terrain: ${planet.terrain}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Population: ${planet.population}", style = MaterialTheme.typography.bodySmall)
            Row {
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (isPinned) "Unpin" else "Pin"
                    )
                }
                IconButton(onClick = onToggleFavourite) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                        tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
