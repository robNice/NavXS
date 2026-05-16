package de.robnice.navxs.overlay

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlaySettings
import de.robnice.navxs.domain.ThemeRegistry
import de.robnice.navxs.ui.settings.resolveThemeIcon

@Composable
fun OverlayRenderer(
    settings: OverlaySettings,
    onButtonPress: (NavButtonType) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        settings.buttons.values.filter { it.active }.forEach { button ->
            val buttonSizeDp = (56 * button.sizePercent / 100).coerceAtLeast(24)
            val iconSizeDp = 32
            val backgroundSizeDp = (iconSizeDp * button.backgroundSizePercent / 100).coerceAtLeast(16)
            Box(
                modifier = Modifier
                    .offset { IntOffset(button.positionXPx, button.positionYPx) }
                    .size(buttonSizeDp.dp)
                    .clickable { onButtonPress(button.type) },
                contentAlignment = Alignment.Center
            ) {
                if (button.backgroundOpacity > 0f) {
                    Box(
                        modifier = Modifier
                            .size(backgroundSizeDp.dp)
                            .background(Color(button.backgroundColorArgb).copy(alpha = button.backgroundOpacity), CircleShape)
                    )
                }
                val icon = resolveThemeIcon(ThemeRegistry().resolve(button.type, button.themeId))
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = button.type.name,
                        tint = Color(button.colorArgb).copy(alpha = button.opacity),
                        modifier = Modifier
                            .size(iconSizeDp.dp)
                            .background(Color.Transparent, CircleShape)
                    )
                } else {
                    Text(fallback(button.type), color = Color(button.colorArgb).copy(alpha = button.opacity))
                }
            }
        }
    }
}

private fun fallback(type: NavButtonType): String = when (type) {
    NavButtonType.BACK -> "<"
    NavButtonType.HOME -> "O"
    NavButtonType.RECENTS -> "[]"
}
