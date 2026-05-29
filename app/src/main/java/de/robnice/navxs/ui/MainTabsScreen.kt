package de.robnice.navxs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.DensityMedium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.robnice.navxs.R
import de.robnice.navxs.ui.apps.AppsScreen
import de.robnice.navxs.ui.settings.SettingsPositionOverlay
import de.robnice.navxs.ui.settings.SettingsScreen
import java.nio.charset.StandardCharsets

@Composable
fun MainTabsScreen(
    modifier: Modifier = Modifier,
    state: MainUiState,
    viewModel: MainViewModel
) {
    val selectedTabIndex = state.selectedTabIndex
    var helpOpen by remember { mutableStateOf(false) }
    var privacyOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
    val privacyMarkdown = remember(configuration.locales[0], context) {
        val language = configuration.locales[0]?.language?.lowercase() ?: ""
        val supported = setOf("de", "fr", "es", "it", "pt", "nl", "pl", "ru", "tr", "ja", "ko", "uk")
        val assetName = if (language in supported) "privacy_policy_$language.md" else "privacy_policy_en.md"
        runCatching {
            context.assets.open(assetName).use { input ->
                input.readBytes().toString(StandardCharsets.UTF_8)
            }
        }.getOrDefault("")
    }
    val darkTheme = MaterialTheme.colorScheme.background.red < 0.5f
    val headerBackground = if (darkTheme) Color(0xFF0D1A2A) else MaterialTheme.colorScheme.surface
    val headerDivider = if (darkTheme) Color(0xFF1B2A3C) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = headerBackground
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MainHeaderWordmark(
                            modifier = Modifier.fillMaxWidth(0.34f)
                        )
                        IconButton(onClick = { helpOpen = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = stringResource(R.string.header_help),
                                tint = if (MaterialTheme.colorScheme.background.red > 0.5f) {
                                    Color(0xFF1976F3)
                                } else {
                                    Color(0xFF39CFFF)
                                }
                            )
                        }
                    }
                    HorizontalDivider(color = headerDivider)
                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = headerBackground,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        divider = {
                            HorizontalDivider(color = headerDivider)
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { viewModel.setSelectedTab(index) },
                                text = { Text(title) }
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedTabIndex) {
                    0 -> AppsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                        state = state,
                        onSearchQueryChange = viewModel::setSearchQuery,
                        onShowSystemAppsChange = viewModel::setShowSystemApps,
                        onAppToggle = viewModel::toggleApp
                    )
                    else -> SettingsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, top = 16.dp, end = 0.dp, bottom = 96.dp),
                        state = state,
                        onSelectButton = viewModel::setSelectedButton,
                        onActiveChange = viewModel::setActive,
                        onColorChange = viewModel::setColor,
                        onOpacityChange = viewModel::setOpacity,
                        onSizeChange = viewModel::setSize,
                        onBackgroundColorChange = viewModel::setBackgroundColor,
                        onBackgroundOpacityChange = viewModel::setBackgroundOpacity,
                        onBackgroundSizeChange = viewModel::setBackgroundSize,
                        onBackgroundSoftnessChange = viewModel::setBackgroundSoftness,
                        onThemeChange = viewModel::setTheme,
                        onOpenEditMode = viewModel::openEditMode
                    )
                }

                AppFooter(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    versionName = versionName,
                    onOpenPrivacy = { privacyOpen = true }
                )
            }
        }

        if (helpOpen) {
            HelpDialog(onDismiss = { helpOpen = false })
        }

        if (privacyOpen) {
            AlertDialog(
                onDismissRequest = { privacyOpen = false },
                confirmButton = {
                    TextButton(onClick = { privacyOpen = false }) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                },
                title = { Text(stringResource(R.string.privacy_dialog_title)) },
                text = {
                    Text(
                        text = markdownToDisplayText(privacyMarkdown),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            )
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
                onPrecisionMove = { type, x, y -> viewModel.setButtonPosition(type, x, y) },
                onResetPosition = viewModel::setButtonPositions
            )
        }
    }
}

@Composable
private fun MainHeaderWordmark(modifier: Modifier = Modifier) {
    val darkTheme = MaterialTheme.colorScheme.background.red < 0.5f
    val navColor = if (darkTheme) Color(0xFFF2F6FC) else Color(0xFF10233F)
    val accentColor = if (darkTheme) Color(0xFF39D7FF) else Color(0xFF1976F3)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = navColor, fontWeight = FontWeight.SemiBold)) {
                    append("Nav")
                }
                withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Normal)) {
                    append("XS")
                }
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                letterSpacing = 0.sp
            ),
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Outlined.DensityMedium,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun AppFooter(
    modifier: Modifier = Modifier,
    versionName: String,
    onOpenPrivacy: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.footer_privacy),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onOpenPrivacy)
            )
            Text(
                text = stringResource(R.string.footer_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun markdownToDisplayText(markdown: String): String {
    return markdown
        .lineSequence()
        .joinToString("\n") { line ->
            when {
                line.startsWith("# ") -> line.removePrefix("# ")
                line.startsWith("## ") -> line.removePrefix("## ")
                else -> line
            }
        }
        .trim()
}
