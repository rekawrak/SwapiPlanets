package com.example.swapiplanets.ui.favourites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swapiplanets.domain.model.FavouriteWithCacheStatus
import com.example.swapiplanets.ui.common.UiState
import com.example.swapiplanets.ui.components.EmptyView
import com.example.swapiplanets.ui.components.ErrorView
import com.example.swapiplanets.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    state: UiState<List<FavouriteWithCacheStatus>>,
    favouriteIds: Set<String>,
    sortNamesDescending: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSortOrderChange: (Boolean) -> Unit,
    onPlanetClick: (String) -> Unit,
    onToggleFavourite: (String) -> Unit
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
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onSortOrderChange(!sortNamesDescending) }) {
                        Text(
                            text = if (sortNamesDescending) "Sort: Z → A" else "Sort: A → Z"
                        )
                    }
                },
                title = { Text("Favourites") }
            )
        }
    ) { innerPadding ->
        when (state) {
            UiState.Loading -> LoadingView(modifier = Modifier.padding(innerPadding))

            UiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyView(message = "Add planets to favourites from list or detail screens.")
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorView(message = state.message, onRetry = onRetry)
                }
            }

            is UiState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.data,
                        key = { it.planet.id }
                    ) { item ->
                        FavouritePlanetItem(
                            item = item,
                            isFavourite = favouriteIds.contains(item.planet.id),
                            onClick = { onPlanetClick(item.planet.id) },
                            onToggleFavourite = { onToggleFavourite(item.planet.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouritePlanetItem(
    item: FavouriteWithCacheStatus,
    isFavourite: Boolean,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val planet = item.planet
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = planet.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (item.isStale) {
                Text(
                    text = "Stale cache — background refresh pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Climate: ${planet.climate}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Terrain: ${planet.terrain}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Population: ${planet.population}",
                style = MaterialTheme.typography.bodySmall
            )
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
