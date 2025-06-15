// File Baru: app/src/main/java/com/psy/deardiary/data/dto/JournalDtos.kt
// Deskripsi: Data Transfer Objects (DTOs) untuk fitur Jurnal. Strukturnya
// cocok dengan skema Pydantic di backend FastAPI.

package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName

// Data yang dikirim ke server saat membuat jurnal baru
data class JournalCreateRequest(
    val title: String,
    val content: String,
    val mood: String,
    val timestamp: Long
)

// Respons yang diterima dari server untuk satu entri jurnal
data class JournalResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("mood")
    val mood: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("owner_id")
    val ownerId: Int,
    @SerializedName("sentiment_score")
    val sentimentScore: String?,
    @SerializedName("key_emotions")
    val keyEmotions: String?
)
