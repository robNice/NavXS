package de.robnice.navxs.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.robnice.navxs.ui.theme.NavXsTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onResume() {
        super.onResume()
        AppForegroundState.setInForeground(true)
        viewModel.refreshAccessibilityStatus()
    }

    override fun onPause() {
        AppForegroundState.setInForeground(false)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavXsTheme {
                val state = viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(state.value.message) {
                    state.value.message?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.consumeMessage()
                    }
                }
                Surface {
                    NavXsApp(
                        state = state.value,
                        viewModel = viewModel,
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    )
                }
            }
        }
    }
}
