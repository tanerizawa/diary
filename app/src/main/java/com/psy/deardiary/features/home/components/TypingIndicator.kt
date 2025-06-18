package com.psy.deardiary.features.home.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    dotColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    dotSize: Dp = 6.dp,
    space: Dp = 4.dp
) {
    val transition = rememberInfiniteTransition(label = "typing")

    val alpha1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(600),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha1"
    )

    val alpha2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(600, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha2"
    )

    val alpha3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(600, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha3"
    )

    Row(modifier = modifier) {
        Dot(alpha = alpha1, color = dotColor, size = dotSize)
        Spacer(Modifier.width(space))
        Dot(alpha = alpha2, color = dotColor, size = dotSize)
        Spacer(Modifier.width(space))
        Dot(alpha = alpha3, color = dotColor, size = dotSize)
    }
}

@Composable
private fun Dot(alpha: Float, color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

