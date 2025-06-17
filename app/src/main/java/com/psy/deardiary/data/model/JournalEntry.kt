// LOKASI: app/src/main/java/com/psy/deardiary/data/model/JournalEntry.kt

package com.psy.deardiary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: Int? = null,
    val title: String,
    val content: String,
    val mood: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tags: List<String>,
    val voiceNotePath: String? = null,
    val isSynced: Boolean = false,

    // --- PENAMBAHAN BARU ---
    // Field untuk menyimpan data dari analisis AI
    val sentimentScore: Float? = null,
    val keyEmotions: String? = null
    // --- AKHIR PENAMBAHAN ---
)
