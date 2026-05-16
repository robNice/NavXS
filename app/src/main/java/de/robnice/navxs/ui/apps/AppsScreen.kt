package de.robnice.navxs.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    onAppToggle: (String, Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val enabledApps = state.installedApps.filter { it.enabled }
    val disabledApps = state.installedApps.filterNot { it.enabled }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                Switch(checked = state.showSystemApps, onCheckedChange = onShowSystemAppsChange)
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
