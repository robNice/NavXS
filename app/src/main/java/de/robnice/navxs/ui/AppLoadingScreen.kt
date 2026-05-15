package de.robnice.navxs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.robnice.navxs.R
import com.google.android.material.loadingindicator.LoadingIndicator

@Composable
fun AppLoadingScreen(
    modifier: Modifier = Modifier
) {
    val indicatorColor = MaterialTheme.colorScheme.primary.toArgb()
    val containerColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { context ->
                LoadingIndicator(context).apply {
                    setIndicatorColor(indicatorColor)
                    setContainerColor(containerColor)
                }
            },
            update = { view ->
                view.setIndicatorColor(indicatorColor)
                view.setContainerColor(containerColor)
            }
        )
        Text(
            text = stringResource(R.string.app_loading),
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
