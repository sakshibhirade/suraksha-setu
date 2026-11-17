package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.SurakshaTheme

@Composable
fun AboutModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = SurakshaTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .border(1.2.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Bar with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "About SurakshaSetu",
                        color = colors.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                // Brand Hero Banner
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.softSurface)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primary)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_suraksha_logo),
                            contentDescription = "SurakshaSetu Cyber Shield Logo",
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SurakshaSetu",
                            color = colors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.4).sp
                        )
                        Text(
                            text = "सुरक्षा सेतु • CYBER DEFENSE PLATFORM",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "v1.0 (Production Release)",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "A mobile-first cybersecurity defense suite designed to protect citizens, detect fraud campaigns, assist incident reporting to I4C/1930, and promote digital literacy.",
                        color = colors.textPrimary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Core Capabilities Grid
                Text(
                    text = "CORE PILLARS & ARCHITECTURE",
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                AboutFeatureRow(
                    icon = Icons.Default.Security,
                    title = "Heuristic Threat Scanner",
                    subtitle = "Offline-first rule engine analyzing phishing SMS, suspicious URLs, fake KYC warnings, and digital arrest threats without data leakage."
                )

                AboutFeatureRow(
                    icon = Icons.Default.Policy,
                    title = "I4C & 1930 Incident Reporting",
                    subtitle = "Structured incident response generating formatted formal complaint drafts compliant with the National Cyber Crime Reporting Portal."
                )

                AboutFeatureRow(
                    icon = Icons.Default.Cloud,
                    title = "Supabase Zero-Trust Cloud DB",
                    subtitle = "Encrypted cloud persistence with PostgreSQL Row-Level Security (RLS), cryptographic UUID mapping, and automated audit logging."
                )

                AboutFeatureRow(
                    icon = Icons.Default.Storage,
                    title = "Offline Room Database Cache",
                    subtitle = "Fast local SQLite storage ensuring continuous usability during low connectivity or network blackouts."
                )

                AboutFeatureRow(
                    icon = Icons.Default.School,
                    title = "Cyber Safety Academy",
                    subtitle = "Interactive awareness modules and scenario-based diagnostic quizzes empowering users against social engineering."
                )

                HorizontalDivider(color = colors.border)

                // Emergency Contacts Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.severityHighContainer)
                        .border(1.dp, colors.severityHigh.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.severityHigh,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "NATIONAL CYBER CRIME HELPLINE",
                            color = colors.severityHigh,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "If you have suffered a financial cyber fraud, dial 1930 immediately to freeze fraudulent transfers within the golden hour.",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Call 1930 Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.severityHigh)
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) { }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = colors.surface, modifier = Modifier.size(15.dp))
                                Text("Call 1930", color = colors.surface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Open Portal Button
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) { }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(15.dp))
                                Text("cybercrime.gov.in", color = colors.textPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AboutFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val colors = SurakshaTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.softSurface)
                .border(1.dp, colors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }
    }
}
