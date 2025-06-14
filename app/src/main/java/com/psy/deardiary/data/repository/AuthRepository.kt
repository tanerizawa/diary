// File: app/src/main/java/com/psy/deardiary/data/repository/AuthRepository.kt
package com.psy.deardiary.data.repository

import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.dto.TokenResponse
import com.psy.deardiary.data.network.AuthApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    val authTokenFlow = userPreferencesRepository.authToken

    suspend fun login(email: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.login(email, password)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    userPreferencesRepository.saveAuthToken(body.accessToken)
                    Result.Success(body)
                } else {
                    Result.Error(response.errorBody()?.string() ?: "Login gagal, silakan coba lagi.")
                }
            } catch (e: HttpException) {
                Result.Error("Terjadi kesalahan pada server. Kode: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            } catch (e: Exception) {
                Result.Error("Terjadi kesalahan yang tidak diketahui.")
            }
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.register(email, password)
                if (response.isSuccessful) {
                    Result.Success(Unit)
                } else {
                    Result.Error(response.errorBody()?.string() ?: "Pendaftaran gagal, email mungkin sudah digunakan.")
                }
            } catch (e: HttpException) {
                Result.Error("Terjadi kesalahan pada server. Kode: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            } catch (e: Exception) {
                Result.Error("Terjadi kesalahan yang tidak diketahui.")
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            userPreferencesRepository.clearAuthToken()
        }
    }
}