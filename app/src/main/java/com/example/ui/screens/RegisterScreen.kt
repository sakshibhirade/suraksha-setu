package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentEntity
import com.example.data.model.ThreatRegisterEntity
import com.example.ui.SurakshaViewModel
import com.example.ui.theme.LightTextBlack
import com.example.ui.theme.SurakshaTheme

@Composable
fun RegisterScreen(
    viewModel: SurakshaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = SurakshaTheme.colors
    val selectedSegment by viewModel.registerTabSelection.collectAsState()
    val severityFilter by viewModel.registerSeverityFilter.collectAsState()
    val registerEntries by viewModel.threatRegisterEntries.collectAsState()
    val myIncidents by viewModel.myIncidents.collectAsState()

    var incidentToDelete by remember { mutableStateOf<IncidentEntity?>(null) }
    var showClearAllIncidentsDialog by remember { mutableStateOf(false) }

    // Confirmation Dialog for deleting single incident report
    if (incidentToDelete != null) {
        val target = incidentToDelete!!
        AlertDialog(
            onDismissRequest = { incidentToDelete = null },
            title = {
                Text(
                    text = "Delete Incident Report?",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete this report (${target.category})? This will remove it from your records.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteIncident(target.id)
                        incidentToDelete = null
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
                TextButton(onClick = { incidentToDelete = null }) {
                    Text("Cancel", color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Confirmation Dialog for clearing all incidents
    if (showClearAllIncidentsDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllIncidentsDialog = false },
            title = {
                Text(
                    text = "Clear All Incident Reports?",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear all your reported incidents? This action cannot be undone.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearMyIncidents()
                        showClearAllIncidentsDialog = false
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
                TextButton(onClick = { showClearAllIncidentsDialog = false }) {
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Threat Register",
                color = colors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                maxLines = 1
            )
            Text(
                text = "Community-verified fraud campaigns and your recorded incidents.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Top Segment Selector (0: Public Registry, 1: My Incidents)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SegmentItem(
                label = "Public Registry (${registerEntries.size})",
                icon = Icons.Default.Public,
                isSelected = selectedSegment == 0,
                onClick = { viewModel.setRegisterTabSelection(0) },
                modifier = Modifier.weight(1f)
            )
            SegmentItem(
                label = "My Incidents (${myIncidents.size})",
                icon = Icons.Default.AssignmentTurnedIn,
                isSelected = selectedSegment == 1,
                onClick = { viewModel.setRegisterTabSelection(1) },
                modifier = Modifier.weight(1f)
            )
        }

        if (selectedSegment == 0) {
            // Severity Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "HIGH", "MEDIUM", "LOW").forEach { filter ->
                    val isSelected = severityFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) (if (colors.isDark) colors.softSurface else colors.accentLime) else colors.surface)
                            .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setSeverityFilter(filter) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("filter_chip_$filter")
                    ) {
                        Text(
                            text = if (filter == "ALL") "All Threats" else "$filter Risk",
                            color = if (isSelected) (if (colors.isDark) colors.primary else LightTextBlack) else colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // List of Threat Register Entries
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(registerEntries, key = { it.id }) { entry ->
                    ThreatRegisterCard(entry = entry)
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        } else {
            // List of User's Reported Incidents
            if (myIncidents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No incidents reported yet.",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Incidents you submit from the Report tab will appear here.",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Header with Clear All Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Submitted Reports (${myIncidents.size})",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showClearAllIncidentsDialog = true }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear All Incidents",
                            tint = colors.severityHigh,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Clear Reports",
                            color = colors.severityHigh,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myIncidents, key = { it.id }) { incident ->
                        MyIncidentCard(
                            incident = incident,
                            onClick = { viewModel.openIncidentSummary(incident) },
                            onDelete = { incidentToDelete = incident }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SurakshaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) (if (colors.isDark) colors.softSurface else colors.accentLime) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) (if (colors.isDark) colors.primary else LightTextBlack) else colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (isSelected) (if (colors.isDark) colors.textPrimary else LightTextBlack) else colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ThreatRegisterCard(entry: ThreatRegisterEntity) {
    val colors = SurakshaTheme.colors

    val severityColor = when (entry.severity) {
        "HIGH" -> colors.severityHigh
        "MEDIUM" -> colors.severityMedium
        else -> colors.severityLow
    }

    val statusBadgeColor = when (entry.status) {
        "verified" -> colors.softLime
        "under_review" -> colors.accentLime
        else -> colors.softSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category & Severity
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(severityColor.copy(alpha = 0.2f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${entry.severity} RISK",
                            color = severityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.softSurface)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = entry.category.replace("_", " ").uppercase(),
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Verification Status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBadgeColor)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (entry.status == "verified") {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = LightTextBlack,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = entry.status.replace("_", " ").replaceFirstChar { it.uppercase() },
                            color = LightTextBlack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title
            Text(
                text = entry.title,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            // Description
            if (entry.description.isNotBlank()) {
                Text(
                    text = entry.description,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun MyIncidentCard(
    incident: IncidentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = SurakshaTheme.colors
    val sevColor = when (incident.riskLevel.uppercase()) {
        "HIGH" -> colors.severityHigh
        "MEDIUM" -> colors.severityMedium
        else -> colors.severityLow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("my_incident_card_${incident.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(sevColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${incident.riskLevel.uppercase()} RISK",
                            color = sevColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = incident.incidentDatetime,
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = incident.category,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = incident.description.take(80) + if (incident.description.length > 80) "..." else "",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Report",
                        tint = colors.severityHigh.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Summary",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
