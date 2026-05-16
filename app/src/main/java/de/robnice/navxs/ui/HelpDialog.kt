package de.robnice.navxs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.robnice.navxs.R
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    // Plain HashMap — only read in click lambdas, no recomposition needed
    val anchors = remember { HashMap<String, Int>() }
    // Viewport top in root coords — captured once at initial layout (scroll = 0)
    val viewportTop = remember { intArrayOf(0) }

    // Captures content-relative Y for a section heading.
    // Formula: screenY + scrollOffset − viewportTop = contentY (constant regardless of scroll)
    fun Modifier.anchor(key: String): Modifier = onGloballyPositioned { coords ->
        anchors[key] = (coords.positionInRoot().y.roundToInt() + scrollState.value - viewportTop[0])
            .coerceAtLeast(0)
    }

    fun scrollTo(key: String) {
        scope.launch { scrollState.animateScrollTo(anchors[key] ?: 0) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.93f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.help_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .onGloballyPositioned { viewportTop[0] = it.positionInRoot().y.roundToInt() }
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    HelpTableOfContents(onScrollTo = ::scrollTo)
                    HelpSectionDivider()

                    HelpSectionTitle(
                        number = "1",
                        text = stringResource(R.string.tab_apps),
                        modifier = Modifier.anchor("apps")
                    )
                    Spacer(Modifier.height(6.dp))
                    HelpBody(stringResource(R.string.help_body_apps))

                    HelpSectionDivider()

                    HelpSectionTitle(
                        number = "2",
                        text = stringResource(R.string.tab_settings),
                        modifier = Modifier.anchor("settings")
                    )
                    Spacer(Modifier.height(14.dp))

                    HelpSubsectionTitle(
                        number = "2.1",
                        text = stringResource(R.string.settings_position_button),
                        modifier = Modifier.anchor("pos")
                    )
                    Spacer(Modifier.height(4.dp))
                    HelpBody(stringResource(R.string.help_body_position))
                    Spacer(Modifier.height(14.dp))

                    HelpSubsectionTitle(
                        number = "2.2",
                        text = stringResource(R.string.help_sub_select),
                        modifier = Modifier.anchor("select")
                    )
                    Spacer(Modifier.height(4.dp))
                    HelpBody(stringResource(R.string.help_body_select))
                    Spacer(Modifier.height(14.dp))

                    HelpSubsectionTitle(
                        number = "2.3",
                        text = stringResource(R.string.settings_active),
                        modifier = Modifier.anchor("active")
                    )
                    Spacer(Modifier.height(4.dp))
                    HelpBody(stringResource(R.string.help_body_active))
                    Spacer(Modifier.height(14.dp))

                    HelpSubsectionTitle(
                        number = "2.4",
                        text = stringResource(R.string.settings_button_section),
                        modifier = Modifier.anchor("button")
                    )
                    Spacer(Modifier.height(6.dp))
                    HelpSettingRow(stringResource(R.string.settings_theme), stringResource(R.string.help_desc_theme))
                    HelpSettingRow(stringResource(R.string.settings_colour), stringResource(R.string.help_desc_colour))
                    HelpSettingRow(stringResource(R.string.settings_opacity), stringResource(R.string.help_desc_opacity))
                    HelpSettingRow(stringResource(R.string.settings_size), stringResource(R.string.help_desc_size))
                    Spacer(Modifier.height(14.dp))

                    HelpSubsectionTitle(
                        number = "2.5",
                        text = stringResource(R.string.settings_button_background_section),
                        modifier = Modifier.anchor("button_bg")
                    )
                    Spacer(Modifier.height(4.dp))
                    HelpBody(stringResource(R.string.help_button_bg_intro))
                    Spacer(Modifier.height(6.dp))
                    HelpSettingRow(stringResource(R.string.settings_button_background_colour), stringResource(R.string.help_desc_bg_colour))
                    HelpSettingRow(stringResource(R.string.settings_button_background_opacity), stringResource(R.string.help_desc_bg_opacity))
                    HelpSettingRow(stringResource(R.string.settings_button_background_size), stringResource(R.string.help_desc_bg_size))
                    HelpSettingRow(stringResource(R.string.settings_button_background_softness), stringResource(R.string.help_desc_bg_softness))

                    HelpSectionDivider()

                    HelpSectionTitle(
                        number = "3",
                        text = stringResource(R.string.help_section_preview),
                        modifier = Modifier.anchor("preview")
                    )
                    Spacer(Modifier.height(6.dp))
                    HelpBody(stringResource(R.string.help_body_preview))

                    Spacer(Modifier.height(16.dp))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpTableOfContents(onScrollTo: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = stringResource(R.string.help_toc_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        TocPrimary("1", stringResource(R.string.tab_apps)) { onScrollTo("apps") }
        TocPrimary("2", stringResource(R.string.tab_settings)) { onScrollTo("settings") }
        TocSecondary("2.1", stringResource(R.string.settings_position_button)) { onScrollTo("pos") }
        TocSecondary("2.2", stringResource(R.string.help_sub_select)) { onScrollTo("select") }
        TocSecondary("2.3", stringResource(R.string.settings_active)) { onScrollTo("active") }
        TocSecondary("2.4", stringResource(R.string.settings_button_section)) { onScrollTo("button") }
        TocSecondary("2.5", stringResource(R.string.settings_button_background_section)) { onScrollTo("button_bg") }
        TocPrimary("3", stringResource(R.string.help_section_preview)) { onScrollTo("preview") }
    }
}

@Composable
private fun TocPrimary(number: String, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TocSecondary(number: String, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HelpSectionTitle(number: String, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HelpSubsectionTitle(number: String, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HelpBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun HelpSettingRow(label: String, description: String) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = onSurface)) {
                append(label)
            }
            withStyle(SpanStyle(color = muted)) {
                append(" — $description")
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun HelpSectionDivider() {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    Spacer(Modifier.height(16.dp))
}
