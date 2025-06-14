// File BARU: app/src/main/java/com/psy/deardiary/data/dto/JournalMappers.kt
// Deskripsi: Berisi fungsi ekstensi untuk memetakan (mengubah) objek
// Data Transfer Objects (DTO) ke model database (Entity) dan sebaliknya.

package com.psy.deardiary.data.dto

import com.psy.deardiary.data.model.JournalEntry

/**
 * Mengubah objek JournalResponse (dari API) menjadi objek JournalEntry (untuk database Room).
 *
 * @return Objek JournalEntry yang siap disimpan ke database lokal.
 */
fun JournalResponse.toJournalEntry(): JournalEntry {
    return JournalEntry(
        remoteId = this.id, // ID dari server disimpan sebagai remoteId
        title = this.title,
        content = this.content,
        mood = this.mood,
        timestamp = this.timestamp,
        tags = this.keyEmotions?.split(",")?.map { it.trim() } ?: emptyList(), // Mengubah string emosi menjadi List
        isSynced = true // Data dari server pasti sudah tersinkronisasi
    )
}

/**
 * Mengubah objek JournalEntry (dari database Room) menjadi objek JournalCreateRequest (untuk dikirim ke API).
 * Ini digunakan saat akan membuat entri baru di server dari data lokal.
 *
 * @return Objek JournalCreateRequest yang siap dikirim melalui Retrofit.
 */
fun JournalEntry.toJournalCreateRequest(): JournalCreateRequest {
    return JournalCreateRequest(
        title = this.title,
        content = this.content,
        mood = this.mood,
        timestamp = this.timestamp
    )
}
