package com.psy.deardiary.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["remoteId", "userId"], unique = true)]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: Int? = null,
    val userId: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val sentimentScore: Float? = null,
    val keyEmotions: String? = null,
    val detectedMood: String? = null,
    val isSynced: Boolean = false,
    val isPlaceholder: Boolean = false
)
