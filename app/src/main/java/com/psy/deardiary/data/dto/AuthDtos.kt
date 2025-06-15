// File Baru: app/src/main/java/com/psy/deardiary/data/dto/AuthDtos.kt
// Deskripsi: Data Transfer Objects (DTOs). Ini adalah data class sederhana yang
// strukturnya cocok persis dengan JSON yang dikirim dan diterima dari backend FastAPI kita.

package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName

/** Request body for user registration */
data class RegisterRequest(
    val email: String,
    val password: String
)

/** Request body for user login */
data class LoginRequest(
    val email: String,
    val password: String
)

/** Response returned by the backend after a successful login */
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

/** Generic error response from the backend */
data class ErrorResponse(
    @SerializedName("detail") val detail: String
)
