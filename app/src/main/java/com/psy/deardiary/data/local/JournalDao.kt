// LOKASI: app/src/main/java/com/psy/deardiary/data/local/JournalDao.kt

package com.psy.deardiary.data.local

import androidx.room.*
import com.psy.deardiary.data.model.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<JournalEntry>

    @Query("UPDATE journal_entries SET remoteId = :newRemoteId, isSynced = 1 WHERE id = :localId")
    suspend fun markAsSynced(localId: Int, newRemoteId: Int)

    @Query("SELECT * FROM journal_entries WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getEntryByRemoteId(remoteId: Int?): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Int): JournalEntry?

    @Query("UPDATE journal_entries SET remoteId = :remoteId, title = :title, content = :content, mood = :mood, timestamp = :timestamp, tags = :tags, isSynced = :isSynced WHERE id = :id")
    suspend fun updateEntry(id: Int, remoteId: Int?, title: String, content: String, mood: String, timestamp: Long, tags: List<String>, isSynced: Boolean)

    @Update
    suspend fun updateLocalEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries()

    @Query("SELECT * FROM journal_entries")
    suspend fun getAllEntriesOnce(): List<JournalEntry>

    // --- PENAMBAHAN BARU ---
    @Query("DELETE FROM journal_entries WHERE id = :localId")
    suspend fun deleteEntryByLocalId(localId: Int)
    // --- AKHIR PENAMBAHAN ---

    @Transaction
    suspend fun upsertAll(entries: List<JournalEntry>) {
        entries.forEach { entry ->
            val existingEntry = getEntryByRemoteId(entry.remoteId)
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
                    isSynced = true
                )
            }
        }
    }
}