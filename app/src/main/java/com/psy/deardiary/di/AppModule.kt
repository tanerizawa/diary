package com.psy.deardiary.di

import android.content.Context
import androidx.room.Room
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.local.AppDatabase
import com.psy.deardiary.data.local.JournalDao
import com.psy.deardiary.data.local.ChatMessageDao
import com.psy.deardiary.data.network.AuthApiService
import com.psy.deardiary.data.network.AuthInterceptor
import com.psy.deardiary.data.network.ChatApiService
import com.psy.deardiary.data.network.JournalApiService
import com.psy.deardiary.data.network.FeedApiService
import com.psy.deardiary.data.network.UserApiService
import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.ChatRepository
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.FeedRepository
import com.psy.deardiary.data.repository.UserRepository
import com.psy.deardiary.BuildConfig
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dear_diary_database"
        )
            .addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5)
            .build()
    }

    @Provides
    @Singleton
    fun provideJournalDao(appDatabase: AppDatabase): JournalDao {
        return appDatabase.journalDao()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(appDatabase: AppDatabase): ChatMessageDao {
        return appDatabase.chatMessageDao()
    }

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
            .baseUrl(BuildConfig.BASE_URL)
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

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedApiService(retrofit: Retrofit): FeedApiService {
        return retrofit.create(FeedApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

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
        journalDao: JournalDao,
        userPreferencesRepository: UserPreferencesRepository
    ): JournalRepository {
        return JournalRepository(journalApiService, journalDao, userPreferencesRepository)
    }
    @Provides
    @Singleton
    fun provideChatRepository(
        chatApiService: ChatApiService,
        chatMessageDao: ChatMessageDao,
        userPreferencesRepository: UserPreferencesRepository
    ): ChatRepository {
        return ChatRepository(chatApiService, chatMessageDao, userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideFeedRepository(
        feedApiService: FeedApiService
    ): FeedRepository {
        return FeedRepository(feedApiService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userApiService: UserApiService
    ): UserRepository {
        return UserRepository(userApiService)
    }
}
