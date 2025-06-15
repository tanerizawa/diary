package com.psy.deardiary.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

interface AuthApiService {
    @POST("api/v1/users/register")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @POST("api/v1/users/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>
}
