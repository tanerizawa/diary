package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName

data class ChatRequest(val message: String)

data class ChatResponse(val reply: String)

data class ChatMessageResponse(
    val id: Int,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    @SerializedName("owner_id") val ownerId: Int
)

data class ChatMessageCreateRequest(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
