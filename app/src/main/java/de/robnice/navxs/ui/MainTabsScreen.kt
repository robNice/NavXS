package de.robnice.navxs.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.robnice.navxs.R
import de.robnice.navxs.ui.apps.AppsScreen
import de.robnice.navxs.ui.settings.SettingsPositionOverlay
import de.robnice.navxs.ui.settings.SettingsScreen

@Composable
fun MainTabsScreen(
    modifier: Modifier = Modifier,
    state: MainUiState,
    viewModel: MainViewModel
) {
    val selectedTabIndex = state.selectedTabIndex
    val tabs = listOf(
        stringResource(R.string.tab_apps),
        stringResource(R.string.tab_settings)
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Image(
                painter = painterResource(R.drawable.navxs_wordmark_white),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.42f)
                    .padding(top = 8.dp, bottom = 4.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = { Text(title) }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> AppsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        state = state,
                        onSearchQueryChange = viewModel::setSearchQuery,
                        onShowSystemAppsChange = viewModel::setShowSystemApps,
                        onAppToggle = viewModel::toggleApp
                    )
                    else -> SettingsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        state = state,
                        onSelectButton = viewModel::setSelectedButton,
                        onActiveChange = viewModel::setActive,
                        onColorChange = viewModel::setColor,
                        onOpacityChange = viewModel::setOpacity,
                        onSizeChange = viewModel::setSize,
                        onThemeChange = viewModel::setTheme,
                        onOpenEditMode = viewModel::openEditMode
                    )
                }
            }
        }

        if (selectedTabIndex == 1 && (state.settings.editMode || state.precisionDialogOpen)) {
            SettingsPositionOverlay(
                settings = state.settings,
                precisionOpen = state.precisionDialogOpen,
                onSelectButton = viewModel::setSelectedButton,
                onCommitMoveButtonPosition = viewModel::setButtonPosition,
                onCloseEditMode = viewModel::closeEditMode,
                onOpenPrecision = { viewModel.setPrecisionDialogOpen(true) },
                onClosePrecision = { viewModel.setPrecisionDialogOpen(false) },
                onStepChange = viewModel::setPrecisionStep,
                onPrecisionMove = { dx, dy -> viewModel.moveSelectedButton(dx.toFloat(), dy.toFloat()) },
                onResetPosition = viewModel::setButtonPositions
            )
        }
    }
}
