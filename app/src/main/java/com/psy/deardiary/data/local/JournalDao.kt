// LOKASI: app/src/main/java/com/psy/deardiary/data/local/JournalDao.kt

package com.psy.deardiary.data.local

import androidx.room.*
import com.psy.deardiary.data.model.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllEntries(userId: Int): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedEntries(userId: Int): List<JournalEntry>

    @Query(
        "UPDATE journal_entries SET remoteId = :newRemoteId, sentimentScore = :sentimentScore, keyEmotions = :keyEmotions, isSynced = 1 WHERE id = :localId"
    )
    suspend fun markAsSynced(
        localId: Int,
        newRemoteId: Int,
        sentimentScore: Float?,
        keyEmotions: String?
    )

    @Query("SELECT * FROM journal_entries WHERE remoteId = :remoteId AND userId = :userId LIMIT 1")
    suspend fun getEntryByRemoteId(remoteId: Int?, userId: Int): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Int): JournalEntry?

    @Query(
        "UPDATE journal_entries SET remoteId = :remoteId, title = :title, content = :content, mood = :mood, timestamp = :timestamp, tags = :tags, sentimentScore = :sentimentScore, keyEmotions = :keyEmotions, isSynced = :isSynced WHERE id = :id"
    )
    suspend fun updateEntry(
        id: Int,
        remoteId: Int?,
        title: String,
        content: String,
        mood: String,
        timestamp: Long,
        tags: List<String>,
        sentimentScore: Float?,
        keyEmotions: String?,
        isSynced: Boolean
    )

    @Update
    suspend fun updateLocalEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE userId = :userId")
    suspend fun deleteAllEntries(userId: Int)

    @Query("SELECT * FROM journal_entries WHERE userId = :userId")
    suspend fun getAllEntriesOnce(userId: Int): List<JournalEntry>

    @Query("SELECT mood FROM journal_entries WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMood(userId: Int): String?

    // --- PENAMBAHAN BARU ---
    @Query("DELETE FROM journal_entries WHERE id = :localId AND userId = :userId")
    suspend fun deleteEntryByLocalId(localId: Int, userId: Int)
    // --- AKHIR PENAMBAHAN ---

    @Transaction
    suspend fun upsertAll(entries: List<JournalEntry>) {
        entries.forEach { entry ->
            val existingEntry = getEntryByRemoteId(entry.remoteId, entry.userId)
            if (existingEntry == null) {
                insertEntry(entry)
            } else {
                updateEntry(
                    id = existingEntry.id,
                    remoteId = entry.remoteId,
                    title = entry.title,
                    content = entry.content,
                    mood = entry.mood,
                    timestamp = entry.timestamp,
                    tags = entry.tags,
                    sentimentScore = entry.sentimentScore,
                    keyEmotions = entry.keyEmotions,
                    isSynced = true
                )
            }
        }
    }
}
