// LOKASI: app/src/main/java/com/psy/deardiary/data/local/AppDatabase.kt

package com.psy.deardiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psy.deardiary.data.model.JournalEntry

// --- PERBAIKAN: Naikkan versi database dari 1 menjadi 2 ---
@Database(entities = [JournalEntry::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
}