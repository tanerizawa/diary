package com.psy.deardiary.data.dto

import com.psy.deardiary.data.model.ChatMessage

fun ChatMessageResponse.toChatMessage(): ChatMessage {
    return ChatMessage(
        remoteId = this.id,
        userId = this.ownerId,
        text = this.text,
        isUser = this.isUser,
        timestamp = this.timestamp,
        sentimentScore = this.sentimentScore,
        keyEmotions = this.keyEmotions,
        detectedMood = this.detectedMood,
        isSynced = true
    )
}

fun ChatMessage.toCreateRequest(): ChatMessageCreateRequest {
    return ChatMessageCreateRequest(
        text = this.text,
        isUser = this.isUser,
        timestamp = this.timestamp,
        sentimentScore = this.sentimentScore,
        keyEmotions = this.keyEmotions,
        detectedMood = this.detectedMood
    )
}
