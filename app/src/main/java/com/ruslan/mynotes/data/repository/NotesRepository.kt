package com.ruslan.mynotes.data.repository

import com.ruslan.mynotes.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeAllNotes(): Flow<List<Note>>
    fun observeNoteById(id: String): Flow<Note?>
    suspend fun storeNoteToCache(note: Note)
    suspend fun removeNoteFromCache(id: String)

    suspend fun syncNotesFromServer(): Result<Unit>
    suspend fun uploadNoteToServer(note: Note): Result<Unit>
    suspend fun deleteNoteOnServer(id: String): Result<Unit>

    suspend fun synchronizeWithServer()
    suspend fun fetchNoteById(id: String, forceRefresh: Boolean = false): Note?
    suspend fun fetchAllNotes(forceRefresh: Boolean = false): List<Note>
}