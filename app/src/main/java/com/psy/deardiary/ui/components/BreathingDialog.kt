package com.psy.deardiary.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BreathingDialog(onDismiss: () -> Unit) {
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        },
        title = { Text("Tarik Napas") },
        text = {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large)
                )
            }
        }
    )
}
