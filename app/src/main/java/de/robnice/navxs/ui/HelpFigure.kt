package de.robnice.navxs.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import de.robnice.navxs.R
import de.robnice.navxs.ui.theme.LocalNavXsDarkTheme

/**
 * A cropped screenshot of the control the surrounding help text talks about. Each figure exists for
 * both colour schemes; the one matching the resolved app scheme is shown, so a forced light or dark
 * scheme never mixes with a screenshot from the other one.
 *
 * Figures are decorative: they are hidden from accessibility services because the text next to them
 * already carries the meaning, and they are small crops, so they never need to be zoomed.
 */
@Composable
private fun HelpFigureImage(
    @DrawableRes light: Int,
    @DrawableRes dark: Int,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = true,
    height: Dp? = null
) {
    val painter = painterResource(if (LocalNavXsDarkTheme.current) dark else light)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = modifier
                    .padding(8.dp)
                    .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                    .then(if (height != null) Modifier.height(height) else Modifier)
            )
        }
    }
}

@Composable
fun HelpFigureAppRow() = HelpFigureImage(
    light = R.drawable.help_fig_app_row_light,
    dark = R.drawable.help_fig_app_row_dark
)

@Composable
fun HelpFigureConfigPicker() = HelpFigureImage(
    light = R.drawable.help_fig_config_picker_light,
    dark = R.drawable.help_fig_config_picker_dark
)

@Composable
fun HelpFigureIndividual() = HelpFigureImage(
    light = R.drawable.help_fig_individual_light,
    dark = R.drawable.help_fig_individual_dark
)

@Composable
fun HelpFigureCopyButton() = HelpFigureImage(
    light = R.drawable.help_fig_copy_button_light,
    dark = R.drawable.help_fig_copy_button_dark
)

@Composable
fun HelpFigurePositionButton() = HelpFigureImage(
    light = R.drawable.help_fig_position_button_light,
    dark = R.drawable.help_fig_position_button_dark
)

@Composable
fun HelpFigureEditorToolbar() = HelpFigureImage(
    light = R.drawable.help_fig_editor_toolbar_light,
    dark = R.drawable.help_fig_editor_toolbar_dark,
    fillWidth = false,
    height = 56.dp
)

@Composable
fun HelpFigureBackgroundButton() = HelpFigureImage(
    light = R.drawable.help_fig_background_button_light,
    dark = R.drawable.help_fig_background_button_dark,
    fillWidth = false,
    height = 40.dp
)

@Composable
fun HelpFigureButtonSelector() = HelpFigureImage(
    light = R.drawable.help_fig_button_selector_light,
    dark = R.drawable.help_fig_button_selector_dark
)

@Composable
fun HelpFigureActiveSwitch() = HelpFigureImage(
    light = R.drawable.help_fig_active_switch_light,
    dark = R.drawable.help_fig_active_switch_dark
)

@Composable
fun HelpFigurePreviewTab() = HelpFigureImage(
    light = R.drawable.help_fig_preview_tab_light,
    dark = R.drawable.help_fig_preview_tab_dark,
    fillWidth = false,
    height = 48.dp
)

@Composable
fun HelpFigureBurnIn() = HelpFigureImage(
    light = R.drawable.help_fig_burn_in_light,
    dark = R.drawable.help_fig_burn_in_dark
)

@Composable
fun HelpFigureColorScheme() = HelpFigureImage(
    light = R.drawable.help_fig_color_scheme_light,
    dark = R.drawable.help_fig_color_scheme_dark
)
