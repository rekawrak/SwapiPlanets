package com.example.swapiplanets.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swapiplanets.domain.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    cacheTtlHours: Int,
    backgroundRefreshEnabled: Boolean,
    profiles: List<UserProfile>,
    activeProfileId: String,
    maxProfiles: Int,
    profileFeedback: String?,
    onBack: () -> Unit,
    onCacheTtlChange: (Int) -> Unit,
    onBackgroundRefreshChange: (Boolean) -> Unit,
    onCreateProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onSwitchProfile: (String) -> Unit
) {
    var newProfileName by rememberSaveable { mutableStateOf("") }
    val canAddProfile = profiles.size < maxProfiles
    val canDeleteProfiles = profiles.size > 1

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
                title = { Text("Settings") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Local profiles",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Favourites, notes, collections and visit history are stored separately for each profile.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Profiles: ${profiles.size}/$maxProfiles",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            profileFeedback?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            profiles.forEach { profile ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = profile.profileId == activeProfileId,
                        onClick = { onSwitchProfile(profile.profileId) },
                        label = { Text(profile.displayName) },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (canDeleteProfiles) {
                        IconButton(onClick = { onDeleteProfile(profile.profileId) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete ${profile.displayName}"
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("New profile name") }
                )
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            onCreateProfile(newProfileName)
                            newProfileName = ""
                        }
                    },
                    enabled = canAddProfile && newProfileName.isNotBlank()
                ) {
                    Text("Add")
                }
            }
            if (!canAddProfile) {
                Text(
                    text = "Profile limit reached. Delete a profile to create a new one.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Cache TTL",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Favourite planets are considered stale after this period and refreshed in background.",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(6, 12, 24, 48).forEach { hours ->
                    FilterChip(
                        selected = cacheTtlHours == hours,
                        onClick = { onCacheTtlChange(hours) },
                        label = { Text("${hours}h") }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Background refresh",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "WorkManager periodically updates stale favourite planets when network is available.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = backgroundRefreshEnabled,
                    onCheckedChange = onBackgroundRefreshChange
                )
            }
        }
    }
}
