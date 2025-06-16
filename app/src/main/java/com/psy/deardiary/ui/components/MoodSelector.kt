// LOKASI BARU: app/src/main/java/com/psy/deardiary/ui/components/MoodSelector.kt
package com.psy.deardiary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun MoodSelector(
    modifier: Modifier = Modifier,
    selectedMood: String,
    onMoodSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    val moods = listOf("😊", "😐", "😟", "😠", "😢")

    Column(modifier) {
        Text(
            "Bagaimana perasaanmu secara umum?",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            moods.forEach { mood ->
                MoodOption(
                    mood = mood,
                    isSelected = mood == selectedMood,
                    onSelect = { onMoodSelected(mood) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun MoodOption(
    mood: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(if (isSelected) 1.2f else 1.0f)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onSelect, enabled = enabled),
        contentAlignment = Alignment.Center
    ) {
        Text(text = mood, style = MaterialTheme.typography.headlineMedium)
    }
}