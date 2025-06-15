package com.psy.deardiary.data.repository

import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.dto.LoginRequest
import com.psy.deardiary.data.dto.RegisterRequest
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
    suspend fun login(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.accessToken
                    userPreferencesRepository.saveAuthToken(token)
                    Result.Success(Unit)
                } else {
                    Result.Error("Login gagal: ${response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Terjadi kesalahan pada server. Kode: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            }
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.register(RegisterRequest(email, password))
                if (response.isSuccessful) {
                    Result.Success(Unit)
                } else {
                    Result.Error("Registrasi gagal: ${response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Terjadi kesalahan pada server. Kode: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            userPreferencesRepository.clearAuthToken()
        }
    }
}
