package com.example.swapiplanets.ui.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swapiplanets.domain.model.PlanetCollectionItem
import com.example.swapiplanets.ui.common.UiState
import com.example.swapiplanets.ui.components.EmptyView
import com.example.swapiplanets.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    state: UiState<List<PlanetCollectionItem>>,
    onBack: () -> Unit,
    onPlanetClick: (String) -> Unit,
    onRemovePlanet: (String) -> Unit
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
                title = { Text("Collection") }
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
                    EmptyView(message = "Add planets from detail screens.")
                }
            }
            is UiState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data, key = { it.planetId }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlanetClick(item.planetId) }
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = item.planetName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(16.dp)
                                )
                                IconButton(
                                    onClick = { onRemovePlanet(item.planetId) },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
            is UiState.Error -> EmptyView(message = state.message)
        }
    }
}
