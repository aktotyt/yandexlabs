package com.ruslan.mynotes.data.repository

import com.ruslan.mynotes.model.Note

interface NotesRepository {
    suspend fun getAllNotes(): List<Note>
    suspend fun getNoteById(id: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
}