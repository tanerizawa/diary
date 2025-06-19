package com.psy.deardiary.fakes

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.local.AppDatabase
import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.JournalRepository
import kotlinx.coroutines.runBlocking

object TestRepositories {
    private val context: Context = ApplicationProvider.getApplicationContext()

    fun authRepository(): AuthRepository {
        val prefs = UserPreferencesRepository(context)
        return AuthRepository(FakeAuthApiService(), FakeUserApiService(), prefs)
    }

    fun journalRepository(): JournalRepository {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val prefs = UserPreferencesRepository(context)
        runBlocking { prefs.saveUserId(1) }
        return JournalRepository(FakeJournalApiService(), db.journalDao(), prefs)
    }
}
