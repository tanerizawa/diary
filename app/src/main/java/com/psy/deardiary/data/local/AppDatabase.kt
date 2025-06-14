// File: app/src/main/java/com/psy/deardiary/data/local/AppDatabase.kt
// Deskripsi: Konfigurasi utama untuk database Room. Mendaftarkan semua entitas dan DAO.

package com.psy.deardiary.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psy.deardiary.data.model.JournalEntry

@Database(entities = [JournalEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dear_diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}