package de.robnice.navxs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DensityMedium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.robnice.navxs.R

@Composable
fun AccessibilityGateScreen(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit,
    onCheckAgain: () -> Unit
) {
    var disclosureChecked by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF08131F),
                        Color(0xFF10253C),
                        Color(0xFF08111B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AccessibilityGateWordmark(
                modifier = Modifier.fillMaxWidth(0.58f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xCC102237)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.disclosure_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFEAF8FF)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DisclosureItem(stringResource(R.string.disclosure_item_1))
                        DisclosureItem(stringResource(R.string.disclosure_item_2))
                        DisclosureItem(stringResource(R.string.disclosure_item_3))
                        DisclosureItem(stringResource(R.string.disclosure_item_4))
                        DisclosureItem(stringResource(R.string.disclosure_item_5))
                        DisclosureItem(stringResource(R.string.disclosure_item_6))
                        DisclosureItem(stringResource(R.string.disclosure_item_7))
                    }
                    HorizontalDivider(color = Color(0x334FD8FF))
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = disclosureChecked,
                            onCheckedChange = { disclosureChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF39CFFF),
                                uncheckedColor = Color(0xFF4FD8FF),
                                checkmarkColor = Color(0xFF08131F)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.disclosure_checkbox),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD3E7F5),
                            modifier = Modifier.padding(start = 12.dp, top = 1.dp)
                        )
                    }
                    Button(
                        onClick = onOpenSettings,
                        enabled = disclosureChecked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (disclosureChecked) Color(0xFF4FD8FF) else Color(0x334FD8FF),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A3554),
                            contentColor = Color(0xFFEAF8FF),
                            disabledContainerColor = Color(0xFF0E1E2F),
                            disabledContentColor = Color(0xFF4A6B87)
                        )
                    ) {
                        Text(stringResource(R.string.disclosure_continue))
                    }
                    Button(
                        onClick = onCheckAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color(0x804FD8FF),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF11253D),
                            contentColor = Color(0xFFEAF8FF)
                        )
                    ) {
                        Text(stringResource(R.string.check_again))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DisclosureItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = Color(0xFF39CFFF),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 1.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFD3E7F5)
        )
    }
}

@Composable
private fun AccessibilityGateWordmark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = Color(0xFFF3F8FE),
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("Nav")
                }
                withStyle(
                    SpanStyle(
                        color = Color(0xFF39CFFF),
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("XS")
                }
            },
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 28.sp,
                letterSpacing = 0.sp
            )
        )
        Icon(
            imageVector = Icons.Outlined.DensityMedium,
            contentDescription = null,
            tint = Color(0xFF39CFFF),
            modifier = Modifier
                .padding(start = 3.dp, top = 1.dp)
                .size(18.dp)
        )
    }
}
