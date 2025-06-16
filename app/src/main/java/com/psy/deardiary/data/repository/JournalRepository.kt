// LOKASI: app/src/main/java/com/psy/deardiary/data/repository/JournalRepository.kt

package com.psy.deardiary.data.repository

import android.util.Log
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

    suspend fun createJournal(
        title: String,
        content: String,
        mood: String,
        voiceNotePath: String?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val newEntry = JournalEntry(
                    title = title,
                    content = content,
                    mood = mood,
                    voiceNotePath = voiceNotePath,
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

    suspend fun updateJournal(
        id: Int,
        title: String,
        content: String,
        mood: String,
        voiceNotePath: String?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val existingEntry = journalDao.getEntryById(id)
                if (existingEntry != null) {
                    val updatedEntry = existingEntry.copy(
                        title = title,
                        content = content,
                        mood = mood,
                        voiceNotePath = voiceNotePath,
                        isSynced = false,
                        timestamp = System.currentTimeMillis()
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

    // --- FUNGSI YANG DIPERBAIKI ---
    suspend fun deleteJournal(localId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            var serverError: String? = null
            try {
                val entryToDelete = journalDao.getEntryById(localId)
                val remoteId = entryToDelete?.remoteId

                // Jika sudah pernah sinkron, coba hapus juga di server
                if (remoteId != null) {
                    try {
                        val response = journalApiService.deleteJournal(remoteId)
                        if (!response.isSuccessful) {
                            serverError = "Gagal menghapus jurnal di server (Kode: ${response.code()}). Data lokal akan tetap dihapus."
                        }
                    } catch (e: HttpException) {
                        serverError = "Terjadi kesalahan pada server saat menghapus (Kode: ${e.code()}). Data lokal akan tetap dihapus."
                    } catch (e: IOException) {
                        serverError = "Tidak dapat terhubung ke server untuk menghapus. Data lokal akan tetap dihapus."
                    }
                }

                // Selalu hapus dari database lokal, apa pun hasil dari server
                journalDao.deleteEntryByLocalId(localId)

                // Jika ada error dari server, kembalikan Error. Jika tidak, Success.
                if (serverError != null) {
                    Result.Error(serverError!!)
                } else {
                    Result.Success(Unit)
                }

            } catch (e: Exception) {
                Result.Error("Terjadi kesalahan saat menghapus data lokal: ${e.message}")
            }
        }
    }

    suspend fun getJournalEntryById(id: Int): JournalEntry? {
        return withContext(Dispatchers.IO) {
            journalDao.getEntryById(id)
        }
    }

    // --- FUNGSI YANG DIPERBAIKI ---
    suspend fun syncPendingJournals(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val unsyncedEntries = journalDao.getUnsyncedEntries()
            val failedSyncs = mutableListOf<String>()

            for (entry in unsyncedEntries) {
                try {
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
                        failedSyncs.add(entry.title.ifBlank { "Entri tanpa judul" })
                    }
                } catch (e: Exception) {
                    Log.e("SyncJournal", "Failed to sync entry ${entry.id}", e)
                    failedSyncs.add(entry.title.ifBlank { "Entri tanpa judul" })
                }
            }

            if (failedSyncs.isNotEmpty()) {
                Result.Error("Gagal menyinkronkan: ${failedSyncs.joinToString(", ")}")
            } else {
                Result.Success(Unit)
            }
        }
    }

    suspend fun deleteAllLocalEntries() {
        withContext(Dispatchers.IO) {
            journalDao.deleteAllEntries()
        }
    }

    suspend fun getAllEntriesOnce(): List<JournalEntry> {
        return withContext(Dispatchers.IO) { journalDao.getAllEntriesOnce() }
    }
}