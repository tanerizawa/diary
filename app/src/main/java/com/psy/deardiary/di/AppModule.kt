// File: main/java/com/psy/deardiary/di/AppModule.kt
package com.psy.deardiary.di

import android.content.Context
import androidx.room.Room
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.local.AppDatabase
import com.psy.deardiary.data.local.JournalDao
import com.psy.deardiary.data.network.AuthApiService
import com.psy.deardiary.data.network.AuthInterceptor
import com.psy.deardiary.data.network.JournalApiService
import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.JournalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Provider untuk Database Lokal (Room) ---

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dear_diary_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideJournalDao(appDatabase: AppDatabase): JournalDao {
        return appDatabase.journalDao()
    }


    // --- Provider untuk Jaringan (Retrofit & OkHttp) ---

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(userPreferencesRepository: UserPreferencesRepository): AuthInterceptor {
        return AuthInterceptor(userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideJournalApiService(retrofit: Retrofit): JournalApiService {
        return retrofit.create(JournalApiService::class.java)
    }

    // --- Provider untuk Repository ---

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService,
        userPreferencesRepository: UserPreferencesRepository
    ): AuthRepository {
        return AuthRepository(authApiService, userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideJournalRepository(
        journalApiService: JournalApiService,
        journalDao: JournalDao
    ): JournalRepository {
        return JournalRepository(journalApiService, journalDao)
    }
    // Tidak perlu secara eksplisit menyediakan JournalEditorViewModel di sini,
    // karena Hilt akan otomatis menyediakan ViewModel dengan @HiltViewModel.
}