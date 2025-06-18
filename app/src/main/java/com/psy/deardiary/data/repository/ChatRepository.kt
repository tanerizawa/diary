package com.psy.deardiary.data.repository

import com.psy.deardiary.data.dto.ChatRequest
import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.data.network.ChatApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatApiService: ChatApiService
) {
    private val history = mutableListOf<ChatMessage>()
    private var nextId = 0

    fun getConversation(): List<ChatMessage> = history.toList()

    fun addMessage(text: String, isUser: Boolean, isPlaceholder: Boolean = false): ChatMessage {
        val message = ChatMessage(nextId++, text, isUser, isPlaceholder)
        history.add(message)
        return message
    }

    fun replaceMessage(id: Int, newText: String) {
        val index = history.indexOfFirst { it.id == id }
        if (index != -1) {
            history[index] = history[index].copy(text = newText, isPlaceholder = false)
        }
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
}
