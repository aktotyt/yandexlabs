package com.ruslan.mynotes.data.source.local.file

import android.content.Context
import android.util.Log
import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.model.toJson
import com.ruslan.mynotes.data.source.local.LocalNoteDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileNoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocalNoteDataSource {

    private companion object {
        private const val NOTES_FILE_NAME = "notes.json"
        private const val MAX_RETRIES = 3
    }

    private val logger = LoggerFactory.getLogger(FileNoteDataSource::class.java)
    private val notesState = MutableStateFlow<List<Note>>(emptyList())
    private val storageFile: File by lazy {
        File(context.filesDir, NOTES_FILE_NAME).apply {
            if (!exists()) {
                try {
                    createNewFile()
                    logger.info("Created new notes file")
                    Log.d("FileNoteDataSource", "Created new notes file")
                } catch (e: IOException) {
                    logger.error("Failed to create notes file", e)
                    Log.e("FileNoteDataSource", "File creation failed", e)
                }
            }
        }
    }

    init {
        loadInitialData()
    }

    override fun observeAllNotes(): Flow<List<Note>> = notesState

    override fun observeNoteById(id: String): Flow<Note?> = notesState.map { notes ->
        notes.find { it.id == id }
    }

    override suspend fun getAllNotes(): List<Note> = notesState.value

    override suspend fun getNoteById(id: String): Note? = withContext(Dispatchers.IO) {
        try {
            notesState.value.find { it.id == id }
        } catch (e: Exception) {
            logError("Error getting note by ID: $id", e)
            null
        }
    }

    override suspend fun insertNote(note: Note): Unit = withContext(Dispatchers.IO) {
        retryOperation(MAX_RETRIES) {
            val updatedNotes = notesState.value.filterNot { it.id == note.id } + note
            saveAllNotesInternal(updatedNotes)
            logInfo("Note saved: ${note.name} (ID: ${note.id})")
        }
    }

    override suspend fun eraseNote(id: String): Unit = withContext(Dispatchers.IO) {
        retryOperation(MAX_RETRIES) {
            val updatedNotes = notesState.value.filterNot { it.id == id }
            saveAllNotesInternal(updatedNotes)
            logInfo("Note deleted: ID $id")
        }
    }

    override suspend fun saveAllNotes(notes: List<Note>): Unit = withContext(Dispatchers.IO) {
        retryOperation(MAX_RETRIES) {
            saveAllNotesInternal(notes)
            logInfo("Saved ${notes.size} notes to cache")
        }
    }

    private suspend fun saveAllNotesInternal(notes: List<Note>) {
        writeToFile(notes)
        notesState.update { notes }
    }

    private fun loadInitialData() {
        try {
            notesState.value = if (!storageFile.exists() || storageFile.length() == 0L) {
                logInfo("No notes found in cache")
                emptyList()
            } else {
                readNotesFromFile()
            }
        } catch (e: Exception) {
            logError("Error loading initial data", e)
            notesState.value = emptyList()
        }
    }

    private fun readNotesFromFile(): List<Note> {
        return try {
            val jsonString = storageFile.readText()
            val jsonArray = JSONArray(jsonString)
            (0 until jsonArray.length()).mapNotNull { i ->
                Note.fromJson(jsonArray.getJSONObject(i))
            }.also {
                logInfo("Loaded ${it.size} notes from cache")
            }
        } catch (e: Exception) {
            logError("Error reading notes from file", e)
            emptyList()
        }
    }

    private suspend fun writeToFile(notes: List<Note>) {
        try {
            val jsonArray = JSONArray().apply {
                notes.forEach { put(it.toJson()) }
            }
            storageFile.writeText(jsonArray.toString())
            logDebug("Notes successfully written to file")
        } catch (e: Exception) {
            logError("Error writing notes to file", e)
            throw e
        }
    }

    private suspend fun <T> retryOperation(
        maxRetries: Int,
        operation: suspend () -> T,
    ): T? {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logError("Operation failed (attempt ${attempt + 1}/$maxRetries)", e)
                if (attempt < maxRetries - 1) {
                    delay((attempt + 1) * 100L)
                }
            }
        }
        lastException?.let { throw it }
        return null
    }

    private fun logInfo(message: String) {
        logger.info(message)
        Log.i("FileNoteDataSource", message)
    }

    private fun logDebug(message: String) {
        logger.debug(message)
        Log.d("FileNoteDataSource", message)
    }

    private fun logError(message: String, e: Exception) {
        logger.error(message, e)
        Log.e("FileNoteDataSource", message, e)
    }
}