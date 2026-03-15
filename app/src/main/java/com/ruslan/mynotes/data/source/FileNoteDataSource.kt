//package com.ruslan.mynotes.data.source
//
//import android.content.Context
//import android.util.Log
//import com.ruslan.mynotes.model.Note
//import com.ruslan.mynotes.model.toJson
//import com.ruslan.mynotes.data.repository.NotesRepository
//import org.json.JSONArray
//import org.slf4j.LoggerFactory
//import java.io.File
//import javax.inject.Inject
//
//class FileNoteDataSource @Inject constructor(
//    private val context: Context
//) : NotesRepository {
//    private val logger = LoggerFactory.getLogger(FileNoteDataSource::class.java)
//    private val file: File by lazy {
//        File(context.filesDir, "notes.json").also {
//            if (!it.exists()) {
//                it.createNewFile()
//            }
//        }
//    }
//
//    override suspend fun getAllNotes(): List<Note> {
//        return try {
//            if (!file.exists() || file.length() == 0L) return emptyList()
//
//            val jsonString = file.readText()
//            val jsonArray = JSONArray(jsonString)
//
//            (0 until jsonArray.length()).mapNotNull { i ->
//                Note.fromJson(jsonArray.getJSONObject(i))
//            }
//        } catch (e: Exception) {
//            logger.error("Error loading notes from file", e)
//            Log.e("FileNoteDataSource", "Load error", e)
//            emptyList()
//        }
//    }
//
//    override suspend fun getNoteById(id: String): Note? {
//        return getAllNotes().find { it.id == id }
//    }
//
//    override suspend fun saveNote(note: Note) {
//        try {
//            val allNotes = getAllNotes().filter { it.id != note.id } + note
//            saveAllNotes(allNotes)
//            logger.info("Note saved: ${note.name} (ID: ${note.id})")
//            Log.d("FileNoteDataSource", "Note saved: ${note.name}")
//        } catch (e: Exception) {
//            logger.error("Error saving note", e)
//            Log.e("FileNoteDataSource", "Save error", e)
//        }
//    }
//
//    override suspend fun deleteNote(id: String) {
//        try {
//            val notes = getAllNotes().filter { it.id != id }
//            saveAllNotes(notes)
//            logger.info("Note deleted: ID $id")
//            Log.d("FileNoteDataSource", "Note deleted: $id")
//        } catch (e: Exception) {
//            logger.error("Error deleting note", e)
//            Log.e("FileNoteDataSource", "Delete error", e)
//        }
//    }
//
//    private suspend fun saveAllNotes(notes: List<Note>) {
//        val jsonArray = JSONArray()
//        notes.forEach { jsonArray.put(it.toJson()) }
//        file.writeText(jsonArray.toString())
//    }
//}