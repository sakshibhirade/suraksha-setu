package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.heuristic.ThreatAssessment
import com.example.ui.SurakshaViewModel
import com.example.ui.theme.LightTextBlack
import com.example.ui.theme.SurakshaTheme

@Composable
fun HomeScreen(
    viewModel: SurakshaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = SurakshaTheme.colors
    val state by viewModel.threatCheckState.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var scanToDelete by remember { mutableStateOf<com.example.data.model.ThreatCheckEntity?>(null) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Clear Scan History?",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear your entire scan history? This action cannot be undone.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearScanHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.severityHigh,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (scanToDelete != null) {
        val target = scanToDelete!!
        AlertDialog(
            onDismissRequest = { scanToDelete = null },
            title = {
                Text(
                    text = "Delete Scan Record?",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this scan entry (${target.threatType})? This cannot be undone.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteThreatCheck(target.id)
                        scanToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.severityHigh,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scanToDelete = null }) {
                    Text("Cancel", color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Threat Scanner",
                color = colors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Analyze messages, links, and documents with instant offline heuristics.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Mode Selector: Message vs URL vs OCR Text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            InputTypeTab(
                label = "Message",
                icon = Icons.Default.Message,
                isSelected = state.inputType == "message",
                onClick = { viewModel.setScannerInputType("message") },
                modifier = Modifier.weight(1f)
            )
            InputTypeTab(
                label = "URL Link",
                icon = Icons.Default.Link,
                isSelected = state.inputType == "url",
                onClick = { viewModel.setScannerInputType("url") },
                modifier = Modifier.weight(1f)
            )
            InputTypeTab(
                label = "OCR Text",
                icon = Icons.Default.DocumentScanner,
                isSelected = state.inputType == "screenshot_text",
                onClick = { viewModel.setScannerInputType("screenshot_text") },
                modifier = Modifier.weight(1f)
            )
        }

        // Mode-Specific Workable Controls & Presets
        when (state.inputType) {
            "url" -> {
                // URL Link Inspection Mode Specific UI
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "URL & Domain Inspector Presets:",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap to test",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SampleChip(label = "🔴 Fake SBI Portal", onClick = { viewModel.loadSampleThreat("url_kyc") })
                        SampleChip(label = "🔴 Free 5G Scam TLD", onClick = { viewModel.loadSampleThreat("url_recharge") })
                        SampleChip(label = "🟠 Bit.ly WFH Link", onClick = { viewModel.loadSampleThreat("url_job") })
                        SampleChip(label = "🟢 Official SBI Site", onClick = { viewModel.loadSampleThreat("url_safe") })
                    }

                    // Live URL Inspector Preview (if input contains URL)
                    val urlInfo = remember(state.inputText) { parseUrlPreview(state.inputText) }
                    if (urlInfo != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.softSurface)
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    imageVector = if (urlInfo.first.contains("HTTPS")) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (urlInfo.first.contains("HTTPS")) colors.accentLime else colors.severityHigh,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Host: ${urlInfo.second}",
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = urlInfo.first,
                                color = if (urlInfo.first.contains("HTTPS")) colors.accentLime else colors.severityHigh,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            "screenshot_text" -> {
                // OCR Screenshot Extractor Mode Specific UI
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OCR Scam Document Templates:",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Extract sample",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SampleChip(label = "📜 Fake Police Warrant", onClick = { viewModel.loadSampleThreat("ocr_warrant") })
                        SampleChip(label = "💸 WhatsApp UPI Slip", onClick = { viewModel.loadSampleThreat("ocr_upi") })
                        SampleChip(label = "🏆 KBC Lottery Winner", onClick = { viewModel.loadSampleThreat("ocr_lottery") })
                        SampleChip(label = "🧾 Verified BBPS Bill", onClick = { viewModel.loadSampleThreat("ocr_safe_invoice") })
                    }
                }
            }

            else -> {
                // Message Mode Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Phishing & SMS Scams:",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap to test",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SampleChip(label = "🔴 SBI KYC SMS", onClick = { viewModel.loadSampleThreat("kyc") })
                        SampleChip(label = "🟠 Telegram Job Scam", onClick = { viewModel.loadSampleThreat("job") })
                        SampleChip(label = "🔴 Power Bill Disconnection", onClick = { viewModel.loadSampleThreat("electricity") })
                        SampleChip(label = "🟢 Safe Delivery Notice", onClick = { viewModel.loadSampleThreat("safe") })
                    }
                }
            }
        }

        // Main Input Card with Quick Clipboard Paste & Clear
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Input header with helper action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (state.inputType) {
                            "url" -> "Link / Domain to Analyze"
                            "screenshot_text" -> "Extracted Document Text"
                            else -> "Message Content / SMS / Email"
                        },
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Quick Paste from Clipboard
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.softSurface)
                            .clickable {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    viewModel.updateScannerInput(clip)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            tint = colors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Paste",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = state.inputText,
                    onValueChange = { viewModel.updateScannerInput(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .testTag("threat_input_field"),
                    placeholder = {
                        Text(
                            text = when (state.inputType) {
                                "url" -> "Enter or paste website URL (e.g. http://sbi-kyc-update.xyz)..."
                                "screenshot_text" -> "Paste or load OCR text from screenshot / official notice..."
                                else -> "Paste suspicious SMS, WhatsApp message, Telegram job offer, or email..."
                            },
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.softSurface,
                        unfocusedContainerColor = colors.softSurface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (state.inputText.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateScannerInput("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Input",
                                    tint = colors.textSecondary
                                )
                            }
                        }
                    }
                )

                // Scan Action CTA Button
                Button(
                    onClick = { viewModel.scanInput() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("scan_now_button"),
                    enabled = state.inputText.isNotBlank() && !state.isScanning,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                        disabledContainerColor = colors.softSurface,
                        disabledContentColor = colors.textSecondary
                    )
                ) {
                    if (state.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing Threat Patterns...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = when (state.inputType) {
                                "url" -> Icons.Default.Language
                                "screenshot_text" -> Icons.Default.DocumentScanner
                                else -> Icons.Default.Security
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (state.inputType) {
                                "url" -> "Inspect Link Safety"
                                "screenshot_text" -> "Scan OCR Document Text"
                                else -> "Scan Message Now"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Assessment Result Card (Segregated Information Breakdown)
        AnimatedVisibility(
            visible = state.latestAssessment != null,
            enter = fadeIn() + slideInVertically()
        ) {
            state.latestAssessment?.let { assessment ->
                AssessmentResultCard(
                    assessment = assessment,
                    onReportIncident = { viewModel.transferScanToReport() },
                    onDismiss = { viewModel.clearScanResult() }
                )
            }
        }

        // Recent Scans Section with Clear History Action
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Recent Scan History",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (scanHistory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.softSurface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${scanHistory.size}",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Clear History Button
                if (scanHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showClearHistoryDialog = true }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear History",
                            tint = colors.severityHigh,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Clear History",
                            color = colors.severityHigh,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (scanHistory.isNotEmpty()) {
                scanHistory.take(5).forEach { check ->
                    RecentScanItem(
                        check = check,
                        onClick = {
                            viewModel.updateScannerInput(check.inputText)
                            viewModel.setScannerInputType(check.inputType)
                            viewModel.scanInput()
                        },
                        onDelete = {
                            scanToDelete = check
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scan history yet. Scan an SMS, URL or document above to record results.",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

private fun parseUrlPreview(url: String): Pair<String, String>? {
    val trimmed = url.trim()
    if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
        return null
    }
    val protocol = if (trimmed.startsWith("https://", ignoreCase = true)) "HTTPS (Encrypted)" else "HTTP (Insecure ⚠️)"
    val clean = trimmed.removePrefix("https://").removePrefix("http://").removePrefix("HTTPS://").removePrefix("HTTP://")
    val domain = clean.substringBefore("/").substringBefore("?").substringBefore(":")
    return Pair(protocol, domain)
}

@Composable
private fun InputTypeTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SurakshaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) (if (colors.isDark) colors.softSurface else colors.accentLime) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) (if (colors.isDark) colors.primary else LightTextBlack) else colors.textSecondary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                color = if (isSelected) (if (colors.isDark) colors.textPrimary else LightTextBlack) else colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SampleChip(
    label: String,
    onClick: () -> Unit
) {
    val colors = SurakshaTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = colors.textPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun AssessmentResultCard(
    assessment: ThreatAssessment,
    onReportIncident: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SurakshaTheme.colors

    val severityColor = when (assessment.severity) {
        "HIGH" -> colors.severityHigh
        "MEDIUM" -> colors.severityMedium
        else -> colors.severityLow
    }

    val containerColor = when (assessment.severity) {
        "HIGH" -> colors.severityHighContainer
        "MEDIUM" -> colors.severityMediumContainer
        else -> colors.severityLowContainer
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.5.dp, severityColor.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header with Risk Score Metric and Severity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Risk Score Badge
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(containerColor)
                            .border(2.dp, severityColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${assessment.riskScore}",
                                color = severityColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "SCORE",
                                color = colors.textSecondary,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(severityColor)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${assessment.severity} RISK",
                                color = if (assessment.severity == "LOW") LightTextBlack else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = assessment.threatType,
                            color = colors.textPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Confidence Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Heuristic Confidence: ${(assessment.confidence * 100).toInt()}% • On-Device Engine",
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Segregated Section 1: Indicators Detected List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.softSurface)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Detected Threat Signals:",
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                assessment.indicators.forEach { indicator ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (assessment.severity == "LOW") Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = severityColor,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = indicator,
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Segregated Section 2: Recommendations List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.softSurface)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Recommended Next Steps:",
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                assessment.recommendations.forEach { rec ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        )
                        Text(
                            text = rec,
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // CTA Action: Report Incident
            Button(
                onClick = onReportIncident,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("report_incident_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.softLime,
                    contentColor = LightTextBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Report as Incident",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RecentScanItem(
    check: com.example.data.model.ThreatCheckEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = SurakshaTheme.colors
    val sevColor = when (check.severity) {
        "HIGH" -> colors.severityHigh
        "MEDIUM" -> colors.severityMedium
        else -> colors.severityLow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.softSurface)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = check.inputType.uppercase(),
                            color = colors.textSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = check.threatType,
                        color = colors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = check.inputText.take(48) + if (check.inputText.length > 48) "..." else "",
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(sevColor.copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${check.riskScore}/100",
                        color = sevColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Delete scan",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
