// LOKASI: app/src/main/java/com/psy/deardiary/data/datastore/UserPreferencesRepository.kt

package com.psy.deardiary.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        // PENAMBAHAN BARU: Key untuk menyimpan nomor kontak darurat
        val EMERGENCY_CONTACT = stringPreferencesKey("emergency_contact_number")
        val USER_ID = intPreferencesKey("user_id")
    }

    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTH_TOKEN]
    }

    // PENAMBAHAN BARU: Flow untuk membaca nomor kontak darurat
    val emergencyContact: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EMERGENCY_CONTACT]
    }

    val userId: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID]
    }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    suspend fun saveUserId(id: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = id
        }
    }

    // PENAMBAHAN BARU: Fungsi untuk menyimpan nomor kontak darurat
    suspend fun saveEmergencyContact(number: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMERGENCY_CONTACT] = number
        }
    }

    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTH_TOKEN)
        }
    }

    suspend fun clearUserId() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_ID)
        }
    }
}
