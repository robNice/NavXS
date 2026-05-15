package de.robnice.navxs.ui.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import de.robnice.navxs.data.models.InstalledAppInfo

@Composable
fun AppListItem(
    app: InstalledAppInfo,
    onToggle: (String, Boolean) -> Unit
) {
    val appIcon = remember(app.packageName, app.icon) {
        app.icon?.toBitmap()?.asImageBitmap()
    }
    ListItem(
        headlineContent = { Text(app.appName) },
        supportingContent = { Text(app.packageName) },
        leadingContent = {
            appIcon?.let {
                Image(
                    bitmap = it,
                    contentDescription = app.appName,
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        trailingContent = {
            Switch(
                checked = app.enabled,
                onCheckedChange = { enabled -> onToggle(app.packageName, enabled) }
            )
        }
    )
}
