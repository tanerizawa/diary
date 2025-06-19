package com.psy.deardiary.data.repository

import com.psy.deardiary.data.dto.ChatRequest
import com.psy.deardiary.data.dto.toCreateRequest
import com.psy.deardiary.data.dto.toChatMessage
import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.data.local.ChatMessageDao
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import com.psy.deardiary.data.network.ChatApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.dto.AiChatResponse

@Singleton
class ChatRepository @Inject constructor(
    private val chatApiService: ChatApiService,
    private val chatMessageDao: ChatMessageDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val messages: Flow<List<ChatMessage>> =
        userPreferencesRepository.userId.flatMapLatest { id ->
            id?.let { chatMessageDao.getAllMessages(it) } ?: flowOf(emptyList())
        }

    val latestSentiment: Flow<Float?> =
        messages.map { history ->
            history.lastOrNull { it.sentimentScore != null }?.sentimentScore
        }

    fun getConversation(): Flow<List<ChatMessage>> =
        messages.onEach { history ->
            if (history.isEmpty()) {
                val shown = userPreferencesRepository.chatOnboardShown.first()
                if (!shown) {
                    addMessage(
                        "Hai! Aku teman bicaramu di sini. Ceritakan apa yang kamu rasakan.",
                        isUser = false
                    )
                    userPreferencesRepository.setChatOnboardShown(true)
                }
            }
        }

    suspend fun refreshMessages(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.getMessages()
                if (response.isSuccessful && response.body() != null) {
                    val uid = userPreferencesRepository.userId.first() ?: return@withContext Result.Error("User not logged in")
                    val messages = response.body()!!.map { it.toChatMessage() }
                    chatMessageDao.upsertAll(messages.map { it.copy(userId = uid) })
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
        val uid = userPreferencesRepository.userId.first() ?: 0
        val message = ChatMessage(
            text = text,
            isUser = isUser,
            isPlaceholder = isPlaceholder,
            isSynced = false,
            userId = uid
        )
        val id = chatMessageDao.insertMessage(message).toInt()
        return message.copy(id = id)
    }

    suspend fun replaceMessage(
        id: Int,
        newText: String,
        sentimentScore: Float? = null,
        keyEmotions: String? = null,
        detectedMood: String? = null
    ) {
        val uid = userPreferencesRepository.userId.first() ?: return
        val existing = chatMessageDao.getMessageById(id, uid) ?: return
        val updated = existing.copy(
            text = newText,
            isPlaceholder = false,
            sentimentScore = sentimentScore,
            keyEmotions = keyEmotions,
            detectedMood = detectedMood
        )
        chatMessageDao.updateMessage(updated)
    }

    suspend fun fetchReply(text: String): Result<AiChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.sendMessage(ChatRequest(text))
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
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

    suspend fun sendMessage(text: String): Result<AiChatResponse> {
        return fetchReply(text)
    }

    suspend fun updateMessageWithReply(id: Int, replyText: String, detectedMood: String? = null) {
        replaceMessage(id, replyText, detectedMood = detectedMood)
    }

    suspend fun syncPendingMessages(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val uid = userPreferencesRepository.userId.first() ?: return@withContext Result.Error("User not logged in")
                val unsynced = chatMessageDao.getUnsyncedMessages(uid)
                for (msg in unsynced) {
                    val response = chatApiService.postMessage(msg.toCreateRequest())
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        chatMessageDao.markAsSynced(
                            msg.id,
                            body.id,
                            body.sentimentScore,
                            body.keyEmotions,
                            body.detectedMood
                        )
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
