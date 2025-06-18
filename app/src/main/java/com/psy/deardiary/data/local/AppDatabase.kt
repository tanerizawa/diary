// LOKASI: app/src/main/java/com/psy/deardiary/data/local/AppDatabase.kt

package com.psy.deardiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.data.model.ChatMessage

// --- PERBAIKAN: Naikkan versi database dari 2 menjadi 3 ---
@Database(
    entities = [JournalEntry::class, ChatMessage::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun chatMessageDao(): ChatMessageDao
}
