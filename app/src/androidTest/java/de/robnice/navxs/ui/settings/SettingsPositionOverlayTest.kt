package de.robnice.navxs.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.robnice.navxs.R
import de.robnice.navxs.data.NavDefaults
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPositionOverlayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backgroundDialogHidesSliderWithoutImageAndStaysOpenAfterSelect() {
        var backgroundRequested = false
        composeRule.setContent {
            MaterialTheme {
                var dialogOpen by remember { mutableStateOf(false) }
                SettingsPositionOverlay(
                    settings = NavDefaults.defaultOverlaySettings().copy(editMode = true),
                    precisionOpen = false,
                    positionBackgroundUri = null,
                    positionBackgroundAlpha = 100,
                    positionBackgroundDialogOpen = dialogOpen,
                    onSelectButton = {},
                    onCommitMoveButtonPosition = { _, _, _ -> },
                    onCloseEditMode = {},
                    onOpenPrecision = {},
                    onClosePrecision = {},
                    onStepChange = {},
                    onPrecisionMove = { _, _, _ -> },
                    onResetPosition = {},
                    onRequestPositionBackground = { backgroundRequested = true },
                    onClearPositionBackground = {},
                    onPositionBackgroundAlphaChange = {},
                    onPositionBackgroundLoadError = {},
                    onPositionBackgroundDialogOpenChange = { dialogOpen = it }
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(
                composeRule.activity.getString(R.string.settings_position_background_manage)
            )
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.settings_position_background_title))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("position_background_opacity_slider").assertDoesNotExist()

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.settings_position_background_select))
            .performClick()
        composeRule.runOnIdle {
            check(backgroundRequested)
        }
        composeRule.onNodeWithTag("position_background_dialog").assertIsDisplayed()
    }

    @Test
    fun backgroundDialogShowsSliderAndResetForSelectedImage() {
        var backgroundCleared = false
        var dialogClosed = false
        composeRule.setContent {
            MaterialTheme {
                SettingsPositionOverlay(
                    settings = NavDefaults.defaultOverlaySettings().copy(editMode = true),
                    precisionOpen = false,
                    positionBackgroundUri = "content://screenshots/example",
                    positionBackgroundAlpha = 60,
                    positionBackgroundDialogOpen = true,
                    onSelectButton = {},
                    onCommitMoveButtonPosition = { _, _, _ -> },
                    onCloseEditMode = {},
                    onOpenPrecision = {},
                    onClosePrecision = {},
                    onStepChange = {},
                    onPrecisionMove = { _, _, _ -> },
                    onResetPosition = {},
                    onRequestPositionBackground = {},
                    onClearPositionBackground = { backgroundCleared = true },
                    onPositionBackgroundAlphaChange = {},
                    onPositionBackgroundLoadError = {},
                    onPositionBackgroundDialogOpenChange = { dialogClosed = !it }
                )
            }
        }

        composeRule.onNodeWithTag("position_background_opacity_slider").assertIsDisplayed()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.settings_position_background_reset))
            .performClick()
        composeRule.runOnIdle {
            check(backgroundCleared)
            check(dialogClosed)
        }
    }

    @Test
    fun selectedBackgroundReplacesDottedBackdrop() {
        val settings = NavDefaults.defaultOverlaySettings().copy(editMode = true)
        val positions = settings.buttons.mapValues { (_, button) ->
            Offset(button.positionXPx.toFloat(), button.positionYPx.toFloat())
        }
        composeRule.setContent {
            MaterialTheme {
                ButtonPreviewArea(
                    settings = settings,
                    showBackground = true,
                    positionBackgroundImage = ImageBitmap(2, 2),
                    buttonPositions = positions,
                    onMoveSelectedButton = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("position_background_image").assertIsDisplayed()
        composeRule.onNodeWithTag("position_background_dots").assertDoesNotExist()
    }
}
