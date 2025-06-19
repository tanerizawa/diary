package com.psy.deardiary.fakes

import com.psy.deardiary.data.dto.LoginRequest
import com.psy.deardiary.data.dto.RegisterRequest
import com.psy.deardiary.data.dto.TokenResponse
import com.psy.deardiary.data.dto.UserProfileResponse
import com.psy.deardiary.data.dto.UserProfileUpdateRequest
import com.psy.deardiary.data.dto.JournalCreateRequest
import com.psy.deardiary.data.dto.JournalResponse
import com.psy.deardiary.data.network.AuthApiService
import com.psy.deardiary.data.network.UserApiService
import com.psy.deardiary.data.network.JournalApiService
import retrofit2.Response

class FakeAuthApiService : AuthApiService {
    override suspend fun register(body: RegisterRequest): Response<Unit> = Response.success(Unit)
    override suspend fun login(body: LoginRequest): Response<TokenResponse> = Response.success(TokenResponse("token", "bearer"))
    override suspend fun deleteAccount(): Response<Unit> = Response.success(Unit)
}

class FakeUserApiService : UserApiService {
    override suspend fun getProfile(): Response<UserProfileResponse> =
        Response.success(UserProfileResponse(id = 1, email = "test@example.com", name = "Test", bio = null))
    override suspend fun updateProfile(body: UserProfileUpdateRequest): Response<UserProfileResponse> =
        Response.success(UserProfileResponse(id = 1, email = "test@example.com", name = body.name, bio = body.bio))
}

class FakeJournalApiService : JournalApiService {
    override suspend fun createJournal(journal: JournalCreateRequest): Response<JournalResponse> =
        Response.success(JournalResponse(id = 1, title = journal.title ?: "", content = journal.content, mood = journal.mood, timestamp = System.currentTimeMillis(), tags = emptyList(), sentimentScore = null, keyEmotions = null))
    override suspend fun getJournals(skip: Int, limit: Int): Response<List<JournalResponse>> = Response.success(emptyList())
    override suspend fun updateJournal(id: Int, journal: JournalCreateRequest): Response<JournalResponse> =
        Response.success(JournalResponse(id = id, title = journal.title ?: "", content = journal.content, mood = journal.mood, timestamp = System.currentTimeMillis(), tags = emptyList(), sentimentScore = null, keyEmotions = null))
    override suspend fun deleteJournal(id: Int): Response<Unit> = Response.success(Unit)
}
