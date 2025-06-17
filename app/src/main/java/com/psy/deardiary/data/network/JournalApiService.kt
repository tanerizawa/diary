// LOKASI: app/src/main/java/com/psy/deardiary/data/network/JournalApiService.kt

package com.psy.deardiary.data.network

import com.psy.deardiary.data.dto.JournalCreateRequest
import com.psy.deardiary.data.dto.JournalResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface JournalApiService {

    @POST("api/v1/journal")
    suspend fun createJournal(@Body journal: JournalCreateRequest): Response<JournalResponse>

    @GET("api/v1/journal")
    suspend fun getJournals(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<JournalResponse>>

    @PUT("api/v1/journal/{id}")
    suspend fun updateJournal(
        @Path("id") id: Int,
        @Body journal: JournalCreateRequest
    ): Response<JournalResponse>

    // --- PENAMBAHAN BARU ---
    @DELETE("api/v1/journal/{id}")
    suspend fun deleteJournal(@Path("id") id: Int): Response<Unit>
    // --- AKHIR PENAMBAHAN ---
}
