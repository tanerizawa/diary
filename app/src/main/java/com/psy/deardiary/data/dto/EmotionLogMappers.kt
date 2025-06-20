package com.psy.deardiary.data.dto

import com.psy.deardiary.data.model.EmotionLog

fun EmotionLogResponse.toEmotionLog(): EmotionLog {
    return EmotionLog(
        id = this.id,
        timestamp = this.timestamp,
        detectedMood = this.detectedMood,
        sourceText = this.sourceText,
        sourceFeature = this.sourceFeature,
        sentimentScore = this.sentimentScore,
        keyEmotionsDetected = this.keyEmotionsDetected
    )
}
