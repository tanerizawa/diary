package com.psy.deardiary.data.repository

import com.psy.deardiary.data.dto.toJournalCreateRequest
import com.psy.deardiary.data.dto.toJournalEntry
import com.psy.deardiary.data.local.JournalDao
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.data.network.JournalApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalApiService: JournalApiService,
    private val journalDao: JournalDao
) {

    val journals: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    suspend fun refreshJournals(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = journalApiService.getJournals()
                if (response.isSuccessful && response.body() != null) {
                    val journalEntities = response.body()!!.map { it.toJournalEntry() }
                    journalDao.upsertAll(journalEntities)
                    Result.Success(Unit)
                } else {
                    Result.Error("Gagal menyegarkan data: ${response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Terjadi kesalahan pada server. Kode: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            }
        }
    }

    suspend fun createJournal(title: String, content: String, mood: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val newEntry = JournalEntry(
                    title = title,
                    content = content,
                    mood = mood,
                    isSynced = false,
                    tags = emptyList()
                )
                journalDao.insertEntry(newEntry)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error("Gagal menyimpan jurnal ke database lokal.")
            }
        }
    }

    suspend fun updateJournal(id: Int, title: String, content: String, mood: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val existingEntry = journalDao.getEntryById(id)
                if (existingEntry != null) {
                    val updatedEntry = existingEntry.copy(
                        title = title,
                        content = content,
                        mood = mood,
                        isSynced = false, // Tandai sebagai belum sinkron ulang
                        timestamp = System.currentTimeMillis() // Perbarui timestamp
                    )
                    journalDao.updateLocalEntry(updatedEntry)
                    Result.Success(Unit)
                } else {
                    Result.Error("Jurnal tidak ditemukan untuk diperbarui.")
                }
            } catch (e: Exception) {
                Result.Error("Gagal memperbarui jurnal di database lokal: ${e.message}")
            }
        }
    }

    suspend fun getJournalEntryById(id: Int): JournalEntry? {
        return withContext(Dispatchers.IO) {
            journalDao.getEntryById(id)
        }
    }

    suspend fun syncPendingJournals(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val unsyncedEntries = journalDao.getUnsyncedEntries()
                for (entry in unsyncedEntries) {
                    val request = entry.toJournalCreateRequest()

                    val response = if (entry.remoteId != null) {
                        journalApiService.updateJournal(entry.remoteId, request)
                    } else {
                        journalApiService.createJournal(request)
                    }


                    if (response.isSuccessful && response.body() != null) {
                        val remoteId = response.body()!!.id
                        journalDao.markAsSynced(localId = entry.id, newRemoteId = remoteId)
                    } else {
                        return@withContext Result.Error("Gagal menyinkronkan entri: ${entry.title}")
                    }
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error("Gagal melakukan sinkronisasi: ${e.message}")
            }
        }
    }
}