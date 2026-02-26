package com.ruslan.mynotes.di

import android.content.Context
import com.ruslan.mynotes.data.repository.NotesRepository
import com.ruslan.mynotes.data.source.FileNoteDataSource
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
    fun provideNotesRepository(@ApplicationContext context: Context): NotesRepository {
        return FileNoteDataSource(context)
    }
}