package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName

data class ChatRequest(val message: String)

data class ChatResponse(
    val reply: String,
    @SerializedName("sentiment_score") val sentimentScore: Float? = null,
    @SerializedName("key_emotions") val keyEmotions: String? = null
)

data class ChatMessageResponse(
    val id: Int,
    val text: String,
    @SerializedName("is_user") val isUser: Boolean,
    val timestamp: Long,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("sentiment_score") val sentimentScore: Float? = null,
    @SerializedName("key_emotions") val keyEmotions: String? = null,
    @SerializedName("detected_mood") val detectedMood: String? = null
)

data class ChatMessageCreateRequest(
    val text: String,
    @SerializedName("is_user") val isUser: Boolean,
    val timestamp: Long,
    @SerializedName("sentiment_score") val sentimentScore: Float? = null,
    @SerializedName("key_emotions") val keyEmotions: String? = null,
    @SerializedName("detected_mood") val detectedMood: String? = null
)

data class AiChatResponse(
    @SerializedName("action") val action: String,
    @SerializedName("text_response") val textResponse: String,
    @SerializedName("detected_mood") val detectedMood: String? = null,
    @SerializedName("sentiment_score") val sentimentScore: Float? = null,
    @SerializedName("key_emotions") val keyEmotions: String? = null,
    @SerializedName("message_id") val messageId: Int? = null,
    @SerializedName("ai_message_id") val replyId: Int? = null
)

data class DeleteMessagesRequest(
    val ids: List<Int>
)
