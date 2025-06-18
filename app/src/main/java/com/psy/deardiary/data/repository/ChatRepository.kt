package com.psy.deardiary.data.repository

import com.psy.deardiary.data.dto.ChatRequest
import com.psy.deardiary.data.dto.toCreateRequest
import com.psy.deardiary.data.dto.toChatMessage
import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.data.local.ChatMessageDao
import kotlinx.coroutines.flow.Flow
import com.psy.deardiary.data.network.ChatApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.psy.deardiary.data.repository.Result

@Singleton
class ChatRepository @Inject constructor(
    private val chatApiService: ChatApiService,
    private val chatMessageDao: ChatMessageDao
) {
    val messages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    fun getConversation(): Flow<List<ChatMessage>> = messages

    suspend fun refreshMessages(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.getMessages()
                if (response.isSuccessful && response.body() != null) {
                    val messages = response.body()!!.map { it.toChatMessage() }
                    chatMessageDao.upsertAll(messages)
                    Result.Success(Unit)
                } else {
                    Result.Error("${'$'}{response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Gagal menyegarkan riwayat: ${'$'}{e.message}")
            }
        }
    }

    suspend fun addMessage(text: String, isUser: Boolean, isPlaceholder: Boolean = false): ChatMessage {
        val message = ChatMessage(
            text = text,
            isUser = isUser,
            isPlaceholder = isPlaceholder,
            isSynced = false
        )
        val id = chatMessageDao.insertMessage(message).toInt()
        return message.copy(id = id)
    }

    suspend fun replaceMessage(id: Int, newText: String) {
        val existing = chatMessageDao.getMessageById(id) ?: return
        val updated = existing.copy(text = newText, isPlaceholder = false)
        chatMessageDao.updateMessage(updated)
    }

    suspend fun fetchReply(text: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.sendMessage(ChatRequest(text))
                if (response.isSuccessful && response.body() != null) {
                    val reply = response.body()!!.reply
                    Result.Success(reply)
                } else {
                    Result.Error("${'$'}{response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Server error: ${'$'}{e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server.")
            }
        }
    }

    suspend fun syncPendingMessages(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val unsynced = chatMessageDao.getUnsyncedMessages()
                for (msg in unsynced) {
                    val response = chatApiService.postMessage(msg.toCreateRequest())
                    if (response.isSuccessful && response.body() != null) {
                        val remoteId = response.body()!!.id
                        chatMessageDao.markAsSynced(msg.id, remoteId)
                    } else {
                        return@withContext Result.Error("Gagal menyinkronkan pesan")
                    }
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error("Gagal sinkronisasi: ${'$'}{e.message}")
            }
        }
    }
}
