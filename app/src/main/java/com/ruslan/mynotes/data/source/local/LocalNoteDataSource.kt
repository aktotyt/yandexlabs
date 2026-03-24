package com.ruslan.mynotes.data.source.local

import com.ruslan.mynotes.data.model.Note
import kotlinx.coroutines.flow.Flow

interface LocalNoteDataSource {
    fun observeAllNotes(): Flow<List<Note>>
    fun observeNoteById(id: String): Flow<Note?>
    suspend fun getAllNotes(): List<Note>
    suspend fun getNoteById(id: String): Note?
    suspend fun insertNote(note: Note)
    suspend fun eraseNote(id: String)
    suspend fun saveAllNotes(notes: List<Note>)
}