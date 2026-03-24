package com.ruslan.mynotes.data.local.room

import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.local.NoteEntity
import com.ruslan.mynotes.data.source.local.LocalNoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomNoteDataSource @Inject constructor(
    private val noteDao: NoteDao
) : LocalNoteDataSource {

    override fun observeAllNotes(): Flow<List<Note>> =
        noteDao.observeAllNotes()
            .map { entities ->
                entities.map { it.toNote() }
                    .sortedByDescending { it.createdAt }
            }

    override fun observeNoteById(id: String): Flow<Note?> =
        noteDao.observeNoteById(id)
            .map { entity -> entity?.toNote() }

    override suspend fun getAllNotes(): List<Note> =
        noteDao.getAllNotes().map { it.toNote() }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toNote()

    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(NoteEntity.fromNote(note))
    }

    override suspend fun eraseNote(id: String) {
        noteDao.getNoteById(id)?.let { noteDao.deleteNote(it) }
    }

    override suspend fun saveAllNotes(notes: List<Note>) {
        noteDao.insertAllNotes(notes.map { NoteEntity.fromNote(it) })
    }

    suspend fun clearDatabase() {
        noteDao.eraseAllNotes()
    }
}