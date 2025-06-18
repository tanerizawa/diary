package com.psy.deardiary.data.repository

import com.psy.deardiary.data.dto.FeedItemResponse
import com.psy.deardiary.data.dto.toFeedItem
import com.psy.deardiary.data.network.FeedApiService
import com.psy.deardiary.features.home.FeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val feedApiService: FeedApiService
) {
    suspend fun getFeed(): Result<List<FeedItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = feedApiService.getFeed()
                if (response.isSuccessful && response.body() != null) {
                    val items = response.body()!!.mapNotNull { it.toFeedItem() }
                    Result.Success(items)
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
