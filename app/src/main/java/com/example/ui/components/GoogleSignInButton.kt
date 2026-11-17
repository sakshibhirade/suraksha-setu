package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SurakshaTheme

@Composable
fun GoogleLogo(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val scaleFactor = size.width / 24f
        scale(scaleFactor, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            // Blue Path (Crossbar and right portion)
            val bluePath = Path().apply {
                moveTo(23.49f, 12.28f)
                cubicTo(23.49f, 11.43f, 23.42f, 10.75f, 23.27f, 10.0f)
                lineTo(12.0f, 10.0f)
                lineTo(12.0f, 14.51f)
                lineTo(18.47f, 14.51f)
                cubicTo(18.18f, 15.99f, 17.33f, 17.24f, 16.07f, 18.09f)
                lineTo(16.07f, 21.11f)
                lineTo(19.95f, 21.11f)
                cubicTo(22.22f, 19.02f, 23.49f, 15.94f, 23.49f, 12.28f)
                close()
            }
            drawPath(path = bluePath, color = Color(0xFF4285F4))

            // Green Path (Bottom)
            val greenPath = Path().apply {
                moveTo(12.0f, 24.0f)
                cubicTo(15.24f, 24.0f, 17.95f, 22.92f, 19.95f, 21.11f)
                lineTo(16.07f, 18.09f)
                cubicTo(14.99f, 18.81f, 13.62f, 19.25f, 12.0f, 19.25f)
                cubicTo(8.88f, 19.25f, 6.23f, 17.15f, 5.28f, 14.32f)
                lineTo(1.24f, 14.32f)
                lineTo(1.24f, 17.44f)
                cubicTo(3.22f, 21.36f, 7.37f, 24.0f, 12.0f, 24.0f)
                close()
            }
            drawPath(path = greenPath, color = Color(0xFF34A853))

            // Yellow Path (Bottom left)
            val yellowPath = Path().apply {
                moveTo(5.28f, 14.32f)
                cubicTo(5.03f, 13.58f, 4.9f, 12.8f, 4.9f, 12.0f)
                cubicTo(4.9f, 11.2f, 5.03f, 10.42f, 5.28f, 9.68f)
                lineTo(5.28f, 6.56f)
                lineTo(1.24f, 6.56f)
                cubicTo(0.45f, 8.13f, 0.0f, 9.91f, 0.0f, 12.0f)
                cubicTo(0.0f, 14.09f, 0.45f, 15.87f, 1.24f, 17.44f)
                lineTo(5.28f, 14.32f)
                close()
            }
            drawPath(path = yellowPath, color = Color(0xFFFBBC05))

            // Red Path (Top)
            val redPath = Path().apply {
                moveTo(12.0f, 4.75f)
                cubicTo(13.77f, 4.75f, 15.35f, 5.36f, 16.6f, 6.55f)
                lineTo(20.03f, 3.12f)
                cubicTo(17.95f, 1.19f, 15.24f, 0.0f, 12.0f, 0.0f)
                cubicTo(7.37f, 0.0f, 3.22f, 2.64f, 1.24f, 6.56f)
                lineTo(5.28f, 9.68f)
                cubicTo(6.23f, 6.85f, 8.88f, 4.75f, 12.0f, 4.75f)
                close()
            }
            drawPath(path = redPath, color = Color(0xFFEA4335))
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Continue with Google",
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val colors = SurakshaTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (colors.isDark) colors.surface else Color.White)
            .border(
                width = 1.2.dp,
                color = if (colors.isDark) colors.border else Color(0xFFDADCE0),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .padding(horizontal = 16.dp)
            .testTag("google_sign_in_button"),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.primary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Signing in with Google...",
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                GoogleLogo(modifier = Modifier.size(20.dp))

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = text,
                    color = if (colors.isDark) Color.White else Color(0xFF3C4043),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
