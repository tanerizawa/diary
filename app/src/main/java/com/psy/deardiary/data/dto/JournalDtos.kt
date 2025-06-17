// LOKASI: app/src/main/java/com/psy/deardiary/data/dto/JournalDtos.kt

package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName

data class JournalResponse(
    val id: Int,
    val title: String?,
    val content: String,
    val mood: String,
    val timestamp: Long,
    @SerializedName("owner_id") val ownerId: Int,

    // --- PERBAIKAN DAN PENAMBAHAN ---
    @SerializedName("sentiment_score") val sentimentScore: Double?,
    // Ubah tipe data dari List<String> menjadi String agar cocok dengan respons API
    @SerializedName("key_emotions") val keyEmotions: String?
    // --- AKHIR PERUBAHAN ---
)

data class JournalCreateRequest(
    val title: String?,
    val content: String,
    val mood: String,
    val timestamp: Long
)
