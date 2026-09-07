package de.robnice.navxs.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.robnice.navxs.R
import de.robnice.navxs.data.models.AppThemeMode
import de.robnice.navxs.ui.MainUiState

@Composable
fun GeneralSettingsScreen(
    modifier: Modifier = Modifier,
    state: MainUiState,
    onBurnInProtectionChange: (Boolean) -> Unit,
    onAppThemeModeChange: (AppThemeMode) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SettingsCard {
                Text(
                    text = stringResource(R.string.general_burn_in_section),
                    style = MaterialTheme.typography.titleMedium
                )
                SettingsDivider()
                SettingHeader(
                    title = stringResource(R.string.general_burn_in_title),
                    subtitle = stringResource(R.string.general_burn_in_description)
                ) {
                    Switch(
                        checked = state.burnInProtectionEnabled,
                        onCheckedChange = onBurnInProtectionChange
                    )
                }
            }
        }

        item {
            SettingsCard {
                Text(
                    text = stringResource(R.string.general_appearance_section),
                    style = MaterialTheme.typography.titleMedium
                )
                SettingsDivider()
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.general_color_scheme),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.general_color_scheme_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = state.appThemeMode == mode,
                                onClick = { onAppThemeModeChange(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = AppThemeMode.entries.size
                                ),
                                label = {
                                    Text(
                                        text = when (mode) {
                                            AppThemeMode.SYSTEM -> stringResource(R.string.general_theme_system)
                                            AppThemeMode.LIGHT -> stringResource(R.string.general_theme_light)
                                            AppThemeMode.DARK -> stringResource(R.string.general_theme_dark)
                                        },
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
