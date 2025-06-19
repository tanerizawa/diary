package com.psy.deardiary.data.network

import com.psy.deardiary.data.dto.ChatRequest
import com.psy.deardiary.data.dto.AiChatResponse
import com.psy.deardiary.data.dto.ChatMessageCreateRequest
import com.psy.deardiary.data.dto.ChatMessageResponse
import com.psy.deardiary.data.dto.DeleteMessagesRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.HTTP

interface ChatApiService {
    @POST("api/v1/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<AiChatResponse>

    @GET("api/v1/chat/messages")
    suspend fun getMessages(): Response<List<ChatMessageResponse>>

    @POST("api/v1/chat/messages")
    suspend fun postMessage(@Body request: ChatMessageCreateRequest): Response<ChatMessageResponse>

    @HTTP(method = "DELETE", path = "api/v1/chat/messages", hasBody = true)
    suspend fun deleteMessages(@Body request: DeleteMessagesRequest): Response<Unit>
}
