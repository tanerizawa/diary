// File: app/src/main/java/com/psy/deardiary/data/model/JournalEntry.kt
// VERSI DIPERBARUI: Ditambahkan kolom voiceNotePath.

package com.psy.deardiary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ID dari server setelah sinkronisasi berhasil. Bisa null jika belum sinkron.
    val remoteId: Int? = null,

    val title: String,
    val content: String,

    // PERBAIKAN: Menambahkan kolom untuk menyimpan path file audio.
    val voiceNotePath: String? = null,

    val mood: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tags: List<String>,

    // Properti untuk journaling terpandu (jika diimplementasikan)
    val isGuided: Boolean = false,
    val guidedPrompt: String? = null,

    // Penanda status sinkronisasi dengan server
    val isSynced: Boolean
)
