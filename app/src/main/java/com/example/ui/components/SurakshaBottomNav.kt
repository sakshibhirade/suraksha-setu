package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.SurakshaTheme

@Composable
fun SurakshaBottomNav(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SurakshaTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(
                label = "Scan",
                iconSelected = Icons.Filled.Security,
                iconUnselected = Icons.Outlined.Security,
                isSelected = selectedTab == AppTab.HOME,
                onClick = { onTabSelected(AppTab.HOME) },
                testTag = "nav_tab_home"
            )

            NavTabItem(
                label = "Report",
                iconSelected = Icons.Filled.ReportProblem,
                iconUnselected = Icons.Outlined.ReportProblem,
                isSelected = selectedTab == AppTab.REPORT,
                onClick = { onTabSelected(AppTab.REPORT) },
                testTag = "nav_tab_report"
            )

            NavTabItem(
                label = "Register",
                iconSelected = Icons.Filled.Assignment,
                iconUnselected = Icons.Outlined.Assignment,
                isSelected = selectedTab == AppTab.REGISTER,
                onClick = { onTabSelected(AppTab.REGISTER) },
                testTag = "nav_tab_register"
            )

            NavTabItem(
                label = "Learn",
                iconSelected = Icons.Filled.AutoStories,
                iconUnselected = Icons.Outlined.AutoStories,
                isSelected = selectedTab == AppTab.LEARN,
                onClick = { onTabSelected(AppTab.LEARN) },
                testTag = "nav_tab_learn"
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    iconSelected: ImageVector,
    iconUnselected: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = SurakshaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) (if (colors.isDark) colors.softSurface else colors.accentLime.copy(alpha = 0.25f)) else colors.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) iconSelected else iconUnselected,
                contentDescription = label,
                tint = if (isSelected) {
                    if (colors.isDark) colors.primary else colors.primary
                } else colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = label.uppercase(),
                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
