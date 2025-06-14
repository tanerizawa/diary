// File: app/src/main/java/com/psy/deardiary/ui/components/Cards.kt
// Deskripsi: Komponen kartu untuk menampilkan preview jurnal di layar utama.

package com.psy.deardiary.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psy.deardiary.ui.theme.DearDiaryTheme
import androidx.compose.foundation.shape.RoundedCornerShape


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryCard(
    title: String,
    contentPreview: String,
    mood: String, // Mood bisa berupa emoji atau teks
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contentPreview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mood,
                    style = MaterialTheme.typography.bodyLarge // Emoji terlihat lebih baik dengan ukuran lebih besar
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Preview untuk melihat komponen di Android Studio tanpa menjalankan aplikasi
@Preview(showBackground = true)
@Composable
fun JournalEntryCardPreview() {
    DearDiaryTheme {
        JournalEntryCard(
            title = "Hari yang Cerah Penuh Harapan",
            contentPreview = "Pagi ini aku bangun dengan perasaan yang sangat ringan. Entah kenapa, semua terasa mungkin. Aku berjalan-jalan di taman dan melihat bunga...",
            mood = "😊",
            date = "14 Juni 2025",
            onClick = {}
        )
    }
}