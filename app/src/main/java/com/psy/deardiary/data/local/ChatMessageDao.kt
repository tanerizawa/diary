package com.psy.deardiary.data.local

import androidx.room.*
import com.psy.deardiary.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getAllMessages(userId: Int): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedMessages(userId: Int): List<ChatMessage>

    @Query(
        "UPDATE chat_messages SET remoteId = :remoteId, sentimentScore = :sentimentScore, keyEmotions = :keyEmotions, detectedMood = :detectedMood, isSynced = 1 WHERE id = :id"
    )
    suspend fun markAsSynced(
        id: Int,
        remoteId: Int,
        sentimentScore: Float?,
        keyEmotions: String?,
        detectedMood: String?
    )

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("SELECT * FROM chat_messages WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getMessageById(id: Int, userId: Int): ChatMessage?

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun deleteAllMessages(userId: Int)

    @Transaction
    suspend fun upsertAll(messages: List<ChatMessage>) {
        messages.forEach { message ->
            val existing = getMessageById(message.id, message.userId)
            if (existing == null) {
                insertMessage(message)
            } else {
                updateMessage(message.copy(id = existing.id))
            }
        }
    }
}
