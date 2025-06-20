package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName

data class EmotionLogResponse(
    val id: Int,
    val timestamp: Long,
    @SerializedName("detected_mood") val detectedMood: String?,
    @SerializedName("source_text") val sourceText: String,
    @SerializedName("source_feature") val sourceFeature: String,
    @SerializedName("sentiment_score") val sentimentScore: Float?,
    @SerializedName("key_emotions_detected") val keyEmotionsDetected: List<String>?
)
