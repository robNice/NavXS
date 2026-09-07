package de.robnice.navxs.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DensityMedium
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import de.robnice.navxs.R
import de.robnice.navxs.ui.MainUiState

@Composable
fun AppsScreen(
    modifier: Modifier = Modifier,
    state: MainUiState,
    onSearchQueryChange: (String) -> Unit,
    onShowSystemAppsChange: (Boolean) -> Unit,
    onRescanApps: () -> Unit,
    onAppToggle: (String, Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val displayedApps = state.installedApps
        .filter { state.showSystemApps || !it.systemApp }
        .filter { app ->
            state.searchQuery.isBlank() ||
                app.appName.contains(state.searchQuery, ignoreCase = true) ||
                app.packageName.contains(state.searchQuery, ignoreCase = true)
        }
    val enabledApps = displayedApps.filter { it.enabled }
    val disabledApps = displayedApps.filterNot { it.enabled }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.appsLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val darkTheme = MaterialTheme.colorScheme.background.red < 0.5f
                val titleColor = if (MaterialTheme.colorScheme.background.red > 0.5f) {
                    Color(0xFF10233F)
                } else {
                    Color(0xFFF2F6FC)
                }
                val accentColor = if (darkTheme) Color(0xFF39D7FF) else Color(0xFF1976F3)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = titleColor, fontWeight = FontWeight.SemiBold)) {
                                    append("Nav")
                                }
                                withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Normal)) {
                                    append("XS")
                                }
                            },
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Outlined.DensityMedium,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(20.dp)
                        )
                    }
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 28.dp)
                            .size(44.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = stringResource(R.string.apps_scanning),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    Text(
                        text = stringResource(R.string.apps_scanning_detail),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        } else {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text(stringResource(R.string.apps_search_hint)) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.apps_show_system_apps)) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRescanApps) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.apps_rescan)
                            )
                        }
                        Switch(checked = state.showSystemApps, onCheckedChange = onShowSystemAppsChange)
                    }
                }
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(
                        text = stringResource(R.string.apps_enabled_section),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (enabledApps.isEmpty()) {
                    item { Text(stringResource(R.string.apps_empty)) }
                }
                items(enabledApps, key = { it.packageName }) { app ->
                    AppListItem(app = app, onToggle = onAppToggle)
                }
                item {
                    HorizontalDivider()
                    Text(
                        text = stringResource(R.string.apps_installed_section),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(disabledApps, key = { it.packageName }) { app ->
                    AppListItem(app = app, onToggle = onAppToggle)
                }
            }
        }
    }
}
