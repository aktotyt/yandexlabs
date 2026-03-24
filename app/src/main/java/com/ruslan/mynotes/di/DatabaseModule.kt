package com.ruslan.mynotes.di

import android.content.Context
import com.ruslan.mynotes.data.repository.NotesRepository
import com.ruslan.mynotes.data.repository.NotesRepositoryImpl
import com.ruslan.mynotes.data.source.local.LocalNoteDataSource
import com.ruslan.mynotes.data.local.room.AppDatabase
import com.ruslan.mynotes.data.local.room.NoteDao
import com.ruslan.mynotes.data.local.room.RoomNoteDataSource
import com.ruslan.mynotes.data.source.remote.RemoteNoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideLocalNoteSource(dao: NoteDao): LocalNoteDataSource {
        return RoomNoteDataSource(dao)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        localSource: LocalNoteDataSource,
        remoteSource: RemoteNoteDataSource
    ): NotesRepository {
        return NotesRepositoryImpl(localSource, remoteSource)
    }
}