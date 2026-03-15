package com.ruslan.mynotes.data.source.local

import android.content.Context
import android.util.Log
import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.model.toJson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val logger = LoggerFactory.getLogger(LocalNoteDataSource::class.java)
    private val storageFile: File by lazy {
        File(context.filesDir, "notes.json").also {
            if (!it.exists()) {
                it.createNewFile()
                logger.info("Created new notes file")
                Log.d("LocalNoteDataSource", "Created new notes file")
            }
        }
    }

    private val notesState = MutableStateFlow<List<Note>>(emptyList())

    init {
        loadInitialData()
    }

    fun observeAllNotes(): Flow<List<Note>> = notesState

    fun observeNoteById(id: String): Flow<Note?> = notesState.map { notes ->
        notes.find { it.id == id }
    }

    suspend fun getAllNotes(): List<Note> = notesState.value

    suspend fun getNoteById(id: String): Note? {
        return try {
            notesState.value.find { it.id == id }
        } catch (e: Exception) {
            logger.error("Error getting note by ID: $id", e)
            Log.e("LocalNoteDataSource", "Error getting note by ID: $id", e)
            null
        }
    }

    suspend fun insertNote(note: Note) {
        try {
            val updatedNotes = notesState.value.filter { it.id != note.id } + note
            saveAllNotes(updatedNotes)
            logger.info("Note saved: ${note.name} (ID: ${note.id})")
            Log.d("LocalNoteDataSource", "Note saved: ${note.name}")
        } catch (e: Exception) {
            logger.error("Error saving note", e)
            Log.e("LocalNoteDataSource", "Save error", e)
        }
    }

    suspend fun eraseNote(id: String) {
        try {
            val updatedNotes = notesState.value.filter { it.id != id }
            saveAllNotes(updatedNotes)
            logger.info("Note deleted: ID $id")
            Log.d("LocalNoteDataSource", "Note deleted: $id")
        } catch (e: Exception) {
            logger.error("Error deleting note", e)
            Log.e("LocalNoteDataSource", "Delete error", e)
        }
    }

    suspend fun saveAllNotes(notes: List<Note>) {
        try {
            writeToFile(notes)
            notesState.update { notes }
            logger.info("Saved ${notes.size} notes to cache")
            Log.d("LocalNoteDataSource", "Saved ${notes.size} notes to cache")
        } catch (e: Exception) {
            logger.error("Error saving all notes", e)
            Log.e("LocalNoteDataSource", "Error saving all notes", e)
        }
    }

    private fun loadInitialData() {
        try {
            val notes = if (!storageFile.exists() || storageFile.length() == 0L) {
                logger.info("No notes found in cache")
                Log.d("LocalNoteDataSource", "No notes found in cache")
                emptyList()
            } else {
                val jsonString = storageFile.readText()
                val jsonArray = JSONArray(jsonString)
                val loadedNotes = (0 until jsonArray.length()).mapNotNull { i ->
                    Note.fromJson(jsonArray.getJSONObject(i))
                }
                logger.info("Loaded ${loadedNotes.size} notes from cache")
                Log.d("LocalNoteDataSource", "Loaded ${loadedNotes.size} notes from cache")
                loadedNotes
            }
            notesState.value = notes
        } catch (e: Exception) {
            logger.error("Error loading initial data", e)
            Log.e("LocalNoteDataSource", "Error loading initial data", e)
            notesState.value = emptyList()
        }
    }

    private suspend fun writeToFile(notes: List<Note>) {
        try {
            val jsonArray = JSONArray()
            notes.forEach { jsonArray.put(it.toJson()) }
            storageFile.writeText(jsonArray.toString())
            logger.debug("Notes successfully written to file")
            Log.d("LocalNoteDataSource", "Notes successfully written to file")
        } catch (e: Exception) {
            logger.error("Error writing notes to file", e)
            Log.e("LocalNoteDataSource", "Error writing notes to file", e)
            throw e
        }
    }
}