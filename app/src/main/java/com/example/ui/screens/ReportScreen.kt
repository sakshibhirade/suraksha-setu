package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentSummary
import com.example.ui.SurakshaViewModel
import com.example.ui.theme.LightTextBlack
import com.example.ui.theme.SurakshaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: SurakshaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = SurakshaTheme.colors
    val context = LocalContext.current
    val state by viewModel.reportState.collectAsState()
    val scrollState = rememberScrollState()

    val categories = listOf(
        "Phishing & Fake Link",
        "UPI & Banking Payment Fraud",
        "Fake Job & Task Scam",
        "Identity & Authority Impersonation",
        "Social Media Extortion / Blackmail",
        "Crypto & Investment Fraud",
        "Other Cyber Incident"
    )

    val platforms = listOf(
        "SMS / Text",
        "WhatsApp",
        "Telegram",
        "Phone Call",
        "Email",
        "Instagram",
        "Web Browser",
        "Other"
    )

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // If an active summary exists, show Summary View; otherwise show Report Form
        if (state.activeSummary != null) {
            IncidentSummaryView(
                summary = state.activeSummary!!,
                complaintDraft = state.complaintDraft,
                onBack = { viewModel.clearActiveSummary() },
                onGenerateDraft = { viewModel.generateComplaintDraft() },
                onUpdateDraft = { viewModel.updateComplaintDraftText(it) },
                onCopyDraft = { draftText ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Cybercrime Complaint Draft", draftText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Complaint draft copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                onShareDraft = { draftText ->
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Formal Cybercrime Complaint Draft")
                        putExtra(Intent.EXTRA_TEXT, draftText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Cyber Complaint via"))
                }
            )
        } else {
            // Screen Header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Report Incident",
                    color = colors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1
                )
                Text(
                    text = "File structured incident facts to generate official complaint documentation.",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Form Container Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Category Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Incident Category *",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = state.category,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .testTag("category_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.softSurface,
                                    unfocusedContainerColor = colors.softSurface,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier.background(colors.surface)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = colors.textPrimary) },
                                        onClick = {
                                            viewModel.updateReportField(category = cat)
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Platform Selection Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Medium / Platform",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            platforms.forEach { platform ->
                                val isSelected = state.platform == platform
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) (if (colors.isDark) colors.softSurface else colors.accentLime) else colors.softSurface)
                                        .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.updateReportField(platform = platform) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = platform,
                                        color = if (isSelected) (if (colors.isDark) colors.primary else LightTextBlack) else colors.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Incident Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Detailed Narrative / What Happened *",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.updateReportField(description = it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("incident_description_input"),
                            placeholder = {
                                Text(
                                    text = "Describe chronologically what message/call you received, what links were clicked, or what was promised...",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
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
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // URL / Link involved
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Suspicious Link / Website URL (if any)",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = state.url,
                            onValueChange = { viewModel.updateReportField(url = it) },
                            modifier = Modifier.fillMaxWidth().testTag("incident_url_input"),
                            placeholder = { Text("e.g. http://sbi-kyc-update.xyz/login", color = colors.textSecondary, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.softSurface,
                                unfocusedContainerColor = colors.softSurface,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Financial Transaction Info
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Transaction / Financial Loss Info (Optional)",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = state.transactionInfo,
                            onValueChange = { viewModel.updateReportField(transactionInfo = it) },
                            modifier = Modifier.fillMaxWidth().testTag("incident_transaction_input"),
                            placeholder = { Text("e.g. UPI Ref: 3189289291, Amount: Rs 15,000 to fraud@okaxis", color = colors.textSecondary, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.softSurface,
                                unfocusedContainerColor = colors.softSurface,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Evidence Note
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Evidence & Technical Identifiers (Optional)",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = state.evidenceNote,
                            onValueChange = { viewModel.updateReportField(evidenceNote = it) },
                            modifier = Modifier.fillMaxWidth().testTag("incident_evidence_input"),
                            placeholder = { Text("e.g. Phone: +91 98200XXXXX, SMS Header: VM-SBIBNK, screenshot saved", color = colors.textSecondary, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.softSurface,
                                unfocusedContainerColor = colors.softSurface,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Error message if any
                    state.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = colors.severityHigh,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Submit Action CTA
                    Button(
                        onClick = { viewModel.submitReport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_report_button"),
                        enabled = state.description.isNotBlank() && !state.isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving & Generating Assessment...")
                        } else {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Incident", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun IncidentSummaryView(
    summary: IncidentSummary,
    complaintDraft: String?,
    onBack: () -> Unit,
    onGenerateDraft: () -> Unit,
    onUpdateDraft: (String) -> Unit,
    onCopyDraft: (String) -> Unit,
    onShareDraft: (String) -> Unit
) {
    val colors = SurakshaTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Back Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Report Form",
                    tint = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Incident Assessment & Action",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INCIDENT #${summary.incidentId}",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (summary.riskLevel == "HIGH") colors.severityHighContainer else colors.severityMediumContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${summary.riskLevel} RISK",
                            color = if (summary.riskLevel == "HIGH") colors.severityHigh else colors.severityMedium,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // What Happened Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "What Happened",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = summary.whatHappened,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                // Timeline & Evidence
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.softSurface)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                            Text("Timeline", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(summary.timeline, color = colors.textSecondary, fontSize = 11.sp)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.softSurface)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.FactCheck, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                            Text("Evidence", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(summary.evidence, color = colors.textSecondary, fontSize = 11.sp, maxLines = 2)
                    }
                }

                // Impact
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Potential Impact Assessment",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = summary.impact,
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }

                // Next Steps Checklist
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Recommended Next Steps",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    summary.nextSteps.forEach { step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Text(
                                text = step,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Generate Complaint Draft
        Button(
            onClick = onGenerateDraft,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("generate_complaint_draft_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Complaint Draft", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        // Complaint Draft Display & Editor Block
        AnimatedVisibility(visible = complaintDraft != null, enter = fadeIn()) {
            complaintDraft?.let { draft ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .border(1.5.dp, colors.accentLime.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Complaint Draft",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Copy Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.softSurface)
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .clickable { onCopyDraft(draft) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("copy_complaint_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = colors.textPrimary, modifier = Modifier.size(13.dp))
                                        Text("Copy", color = colors.textPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }

                                // Share Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.accentLime)
                                        .clickable { onShareDraft(draft) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("share_complaint_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = LightTextBlack, modifier = Modifier.size(13.dp))
                                        Text("Share", color = LightTextBlack, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = draft,
                            onValueChange = onUpdateDraft,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .testTag("complaint_draft_editor"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.softSurface,
                                unfocusedContainerColor = colors.softSurface,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }
    }
}
