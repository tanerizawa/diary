package com.psy.deardiary.data.local

import androidx.room.*
import com.psy.deardiary.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<ChatMessage>

    @Query("UPDATE chat_messages SET remoteId = :remoteId, isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int, remoteId: Int)

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Int): ChatMessage?

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    @Transaction
    suspend fun upsertAll(messages: List<ChatMessage>) {
        messages.forEach { message ->
            val existing = getMessageById(message.id)
            if (existing == null) {
                insertMessage(message)
            } else {
                updateMessage(message.copy(id = existing.id))
            }
        }
    }
}
