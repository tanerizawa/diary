// LOKASI: app/src/main/java/com/psy/deardiary/data/local/AppDatabase.kt

package com.psy.deardiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.data.model.ChatMessage

// --- PERBAIKAN: Naikkan versi database dari 2 menjadi 3 ---
@Database(
    entities = [JournalEntry::class, ChatMessage::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN userId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN userId INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN sentimentScore REAL")
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN keyEmotions TEXT")
            }
        }
    }
}
