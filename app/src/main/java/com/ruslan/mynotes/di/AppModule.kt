package com.ruslan.mynotes.di

import android.content.Context
import com.ruslan.mynotes.data.repository.NotesRepository
import com.ruslan.mynotes.data.repository.NotesRepositoryImpl
import com.ruslan.mynotes.data.source.local.LocalNoteDataSource
import com.ruslan.mynotes.data.source.remote.RemoteNoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideLocalNoteSource(@ApplicationContext context: Context): LocalNoteDataSource {
        return LocalNoteDataSource(context)
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