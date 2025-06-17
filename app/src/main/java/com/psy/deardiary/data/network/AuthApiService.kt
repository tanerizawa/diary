// Lokasi: app/src/main/java/com/psy/deardiary/data/network/AuthApiService.kt
// Deskripsi: Menambahkan definisi untuk endpoint DELETE.

package com.psy.deardiary.data.network

import com.psy.deardiary.data.dto.LoginRequest
import com.psy.deardiary.data.dto.RegisterRequest
import com.psy.deardiary.data.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/users/register")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @POST("api/v1/users/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @DELETE("api/v1/users/me")
    suspend fun deleteAccount(): Response<Unit>
}
