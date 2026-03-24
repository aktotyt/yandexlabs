package com.ruslan.mynotes.data.repository

import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.source.local.LocalNoteDataSource
import com.ruslan.mynotes.data.source.remote.RemoteNoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val localSource: LocalNoteDataSource,
    private val remoteSource: RemoteNoteDataSource
) : NotesRepository {
    private val syncLock = Mutex()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        initializeRepository()
    }

    private fun initializeRepository() {
        repositoryScope.launch {
            try {
                remoteSource.init()
                synchronizeWithServer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
        withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                syncLock.withLock {
                    try {
                        remoteSource.sendNote(note)
                        Result.success(Unit)
                    } catch (e: Exception) {
                        if (e is HttpException && e.code() == 400) {
                            remoteSource.modifyNote(note)
                            Result.success(Unit)
                        } else {
                            throw e
                        }
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteNoteOnServer(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                syncLock.withLock {
                    remoteSource.removeNote(id)
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun synchronizeWithServer() {
        withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                val remoteNotes = remoteSource.loadNotes()
                localSource.saveAllNotes(remoteNotes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun fetchNoteById(id: String, forceRefresh: Boolean): Note? {
        return withContext(Dispatchers.IO) {
            if (forceRefresh) {
                syncNotesFromServer()
            }
            localSource.getNoteById(id) ?: run {
                syncNotesFromServer()
                localSource.getNoteById(id)
            }
        }
    }

    override suspend fun fetchAllNotes(forceRefresh: Boolean): List<Note> {
        return withContext(Dispatchers.IO) {
            if (forceRefresh || localSource.getAllNotes().isEmpty()) {
                syncNotesFromServer()
            }
            localSource.getAllNotes()
        }
    }
}