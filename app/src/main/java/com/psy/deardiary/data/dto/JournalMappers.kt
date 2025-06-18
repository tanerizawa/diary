// LOKASI: app/src/main/java/com/psy/deardiary/data/dto/JournalMappers.kt

package com.psy.deardiary.data.dto

import com.psy.deardiary.data.model.JournalEntry

fun JournalResponse.toJournalEntry(): JournalEntry {
    return JournalEntry(
        remoteId = this.id,
        userId = this.ownerId,
        title = this.title ?: "",
        content = this.content,
        mood = this.mood,
        timestamp = this.timestamp,
        isSynced = true,
        tags = emptyList(), // Tags bisa di-handle nanti jika diperlukan

        // --- PENAMBAHAN BARU ---
        // Memetakan field baru dari respons API ke model lokal
        sentimentScore = this.sentimentScore?.toFloat(),
        keyEmotions = this.keyEmotions
        // --- AKHIR PENAMBAHAN ---
    )
}

fun JournalEntry.toJournalCreateRequest(): JournalCreateRequest {
    return JournalCreateRequest(
        title = this.title,
        content = this.content,
        mood = this.mood,
        timestamp = this.timestamp
    )
}
