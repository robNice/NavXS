package de.robnice.navxs.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NavXsApp(
    state: MainUiState,
    viewModel: MainViewModel,
    snackbarHost: @Composable () -> Unit
) {
    val positionBackgroundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(viewModel::setPositionBackground)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = snackbarHost
    ) { innerPadding ->
        if (!state.accessibilityCheckComplete) {
            AppLoadingScreen(modifier = Modifier.padding(innerPadding))
        } else if (!state.accessibilityEnabled) {
            AccessibilityGateScreen(
                modifier = Modifier.padding(innerPadding),
                onOpenSettings = viewModel::openAccessibilitySettings,
                onCheckAgain = viewModel::refreshAccessibilityStatus
            )
        } else {
            MainTabsScreen(
                modifier = Modifier.fillMaxSize(),
                state = state,
                viewModel = viewModel,
                onRequestPositionBackground = {
                    positionBackgroundPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }
    }
}
