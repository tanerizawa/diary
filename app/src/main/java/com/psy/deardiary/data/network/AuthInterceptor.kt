// File Baru: app/src/main/java/com/psy/deardiary/data/network/AuthInterceptor.kt
// Deskripsi: Interceptor OkHttp yang secara otomatis menambahkan header
// "Authorization" dengan token Bearer ke setiap panggilan API yang memerlukannya.

package com.psy.deardiary.data.network

import com.psy.deardiary.data.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Ambil token dari DataStore. runBlocking digunakan di sini karena interceptor
        // bersifat sinkron, ini adalah salah satu kasus penggunaan yang diterima.
        val token = runBlocking {
            userPreferencesRepository.authToken.first()
        }

        val requestBuilder = chain.request().newBuilder()

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}