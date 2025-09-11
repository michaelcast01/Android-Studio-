package com.example.tiendasuplementacion.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

/**
 * Reusable shimmer placeholder. Colors and speed can be adjusted to match app theme.
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    durationMillis: Int = 700
) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(translateAnim - 1000f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(modifier = modifier.background(brush))
}
