package de.robnice.navxs.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.unit.dp
import de.robnice.navxs.R
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.domain.ThemeRegistry

@Composable
fun ButtonSelector(
    selectedType: NavButtonType,
    enabled: Boolean = true,
    onSelected: (NavButtonType) -> Unit
) {
    Surface(
        modifier = Modifier.alpha(if (enabled) 1f else 0.45f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            NavButtonType.entries.forEach { type ->
                val selected = type == selectedType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(enabled = enabled) { onSelected(type) }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(2.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 0.dp,
                            color = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        // Symbol oben ueber die volle Breite, Beschriftung darunter einzeilig:
                        // nebeneinander bricht sie in Sprachen wie tr ("Son Uygulamalar") um.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            resolveThemeIcon(ThemeRegistry().fallbackTheme(type))?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = when (type) {
                                    NavButtonType.BACK -> stringResource(R.string.button_back)
                                    NavButtonType.HOME -> stringResource(R.string.button_home)
                                    NavButtonType.RECENTS -> stringResource(R.string.button_recents)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
