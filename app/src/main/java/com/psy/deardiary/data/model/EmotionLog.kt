package com.psy.deardiary.data.model

data class EmotionLog(
    val id: Int,
    val timestamp: Long,
    val detectedMood: String?,
    val sourceText: String,
    val sourceFeature: String,
    val sentimentScore: Float?,
    val keyEmotionsDetected: List<String>?
)
