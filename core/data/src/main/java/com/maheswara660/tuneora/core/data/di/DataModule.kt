package com.maheswara660.tuneora.core.data.di

import android.content.Context
import androidx.room.Room
import com.maheswara660.tuneora.core.data.local.TuneoraDatabase
import com.maheswara660.tuneora.core.data.local.dao.DirectoryDao
import com.maheswara660.tuneora.core.data.local.dao.PlaylistDao
import com.maheswara660.tuneora.core.data.local.dao.SearchHistoryDao
import com.maheswara660.tuneora.core.data.local.dao.SongDao
import com.maheswara660.tuneora.core.data.repository.LocalPreferencesRepository
import com.maheswara660.tuneora.core.data.repository.LocalSearchHistoryRepository
import com.maheswara660.tuneora.core.data.repository.OfflinePlaylistRepository
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import com.maheswara660.tuneora.core.data.repository.PlaylistRepository
import com.maheswara660.tuneora.core.data.repository.SearchHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TuneoraDatabase {
        return Room.databaseBuilder(
            context,
            TuneoraDatabase::class.java,
            "tuneora.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }


    @Provides
    fun provideSongDao(database: TuneoraDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    fun provideDirectoryDao(database: TuneoraDatabase): DirectoryDao {
        return database.directoryDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: TuneoraDatabase): SearchHistoryDao {

        return database.searchHistoryDao()
    }

    @Provides
    fun providePlaylistDao(database: TuneoraDatabase): PlaylistDao {
        return database.playlistDao()
    }


    @Provides
    @Singleton
    fun providePreferencesRepository(
        localPreferencesRepository: LocalPreferencesRepository
    ): PreferencesRepository = localPreferencesRepository

    @Provides
    @Singleton
    fun provideSearchHistoryRepository(
        localSearchHistoryRepository: LocalSearchHistoryRepository
    ): SearchHistoryRepository = localSearchHistoryRepository

    @Provides
    @Singleton
    fun providePlaylistRepository(
        offlinePlaylistRepository: OfflinePlaylistRepository
    ): PlaylistRepository = offlinePlaylistRepository
}




