// File: app/src/main/java/com/psy/deardiary/data/network/AuthApiService.kt
package com.psy.deardiary.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// ---- Data class untuk permintaan (request) ----
data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

// ---- Data class untuk respons login (contoh, sesuaikan dengan backend Anda) ----
data class LoginResponse(
    val access_token: String,
    val token_type: String
    // tambahkan field lain jika ada, misal "user", "refresh_token", dsb.
)

// ---- Interface Retrofit ----
interface AuthApiService {

    @POST("api/v1/users/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<Unit>
    // Jika backend mengembalikan respons selain 204/201 kosong, ganti Unit dengan data class sesuai responsnya

    @POST("api/v1/users/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>
}
