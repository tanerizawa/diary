// LOKASI BARU: app/src/main/java/com/psy/deardiary/ui/components/VoiceJournal.kt
package com.psy.deardiary.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.psy.deardiary.ui.theme.Crisis

@Composable
fun VoiceJournalSection(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    hasRecording: Boolean,
    isPlaying: Boolean,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    onPlaybackClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_animation")
    val recordingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = ""
    )

    Column(modifier = modifier) {
        Text(
            "Rekam Jurnal Suara (Opsional)",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when {
                    isRecording -> "Merekam..."
                    hasRecording -> "Rekaman Tersimpan"
                    else -> "Mulai merekam jurnal suaramu"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = hasRecording && !isRecording) {
                    IconButton(onClick = onPlaybackClick, enabled = !isRecording) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Jeda Pemutaran" else "Putar Rekaman"
                        )
                    }
                }
                IconButton(
                    onClick = if (isRecording) onStopClick else onRecordClick,
                    enabled = !isPlaying
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording) Crisis.copy(alpha = recordingAlpha) else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Berhenti Merekam" else "Mulai Merekam",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}