package com.ruslan.mynotes.data.repository

import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.source.local.LocalNoteDataSource
import com.ruslan.mynotes.data.source.remote.RemoteNoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val localSource: LocalNoteDataSource,
    private val remoteSource: RemoteNoteDataSource
) : NotesRepository {

    override fun observeAllNotes(): Flow<List<Note>> =
        localSource.observeAllNotes().flowOn(Dispatchers.IO)

    override fun observeNoteById(id: String): Flow<Note?> =
        localSource.observeNoteById(id).flowOn(Dispatchers.IO)

    override suspend fun storeNoteToCache(note: Note) =
        withContext(Dispatchers.IO) { localSource.insertNote(note) }

    override suspend fun removeNoteFromCache(id: String) =
        withContext(Dispatchers.IO) { localSource.eraseNote(id) }

    override suspend fun syncNotesFromServer(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val notes = remoteSource.loadNotes()
                localSource.saveAllNotes(notes)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun uploadNoteToServer(note: Note): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                remoteSource.sendNote(note)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteNoteOnServer(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                remoteSource.removeNote(id)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun synchronizeWithServer() {
        withContext(Dispatchers.IO) {
            try {
                val notes = remoteSource.loadNotes()
                localSource.saveAllNotes(notes)
            } catch (e: Exception) {
            }
        }
    }

    override suspend fun fetchNoteById(id: String, forceRefresh: Boolean): Note? {
        return if (forceRefresh) {
            syncNotesFromServer()
            localSource.getNoteById(id)
        } else {
            localSource.getNoteById(id) ?: run {
                syncNotesFromServer()
                localSource.getNoteById(id)
            }
        }
    }

    override suspend fun fetchAllNotes(forceRefresh: Boolean): List<Note> {
        return if (forceRefresh) {
            syncNotesFromServer()
            localSource.getAllNotes()
        } else {
            localSource.getAllNotes().ifEmpty {
                syncNotesFromServer()
                localSource.getAllNotes()
            }
        }
    }
}