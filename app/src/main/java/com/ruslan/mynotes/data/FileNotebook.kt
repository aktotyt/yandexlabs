package com.ruslan.mynotes.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.model.toJson
import org.json.JSONArray
import org.slf4j.LoggerFactory
import java.io.File

class FileNotebook {
    private var items = mutableStateListOf<Note>()
    val allNotes: List<Note>
        get() = items

    private val logger = LoggerFactory.getLogger(FileNotebook::class.java)

    fun put(noteItem: Note) {
        items.add(noteItem)
        logger.info("Note added: title=${noteItem.name}, id=${noteItem.id}, total=${items.size}")
        Log.d("FileNotebook", "Note added: ${noteItem.name}")
    }

    fun delete(targetId: String): Boolean {
        val note = items.find { it.id == targetId }
        val result = items.removeIf { it.id == targetId }
        if (result) {
            logger.info("Note removed: title=${note?.name}, id=$targetId, remaining=${items.size}")
            Log.d("FileNotebook", "Note removed: ${note?.name}")
        } else {
            logger.warn("Note not found for removal: id=$targetId")
            Log.w("FileNotebook", "Note not found: $targetId")
        }
        return result
    }

    fun store(target: File): Boolean {
        return try {
            val container = JSONArray()
            items.forEach { item ->
                container.put(item.toJson())  // теперь это функция, со скобками
            }
            target.writeText(container.toString())
            logger.info("Saved ${items.size} notes to file: ${target.absolutePath}")
            Log.i("FileNotebook", "Notes saved to file")
            true
        } catch (e: Exception) {
            logger.error("Error saving notes to file: ${target.absolutePath}", e)
            Log.e("FileNotebook", "Save error", e)
            false
        }
    }

    fun restore(source: File): Boolean {
        return try {
            if (!source.exists()) {
                logger.warn("File does not exist: ${source.absolutePath}")
                Log.w("FileNotebook", "File not found: ${source.absolutePath}")
                return false
            }
            val raw = source.readText()
            val container = JSONArray(raw)
            items.clear()
            for (idx in 0 until container.length()) {
                Note.fromJson(container.getJSONObject(idx))?.let { recovered ->
                    items.add(recovered)
                }
            }
            logger.info("Loaded ${items.size} notes from file: ${source.absolutePath}")
            Log.i("FileNotebook", "Notes loaded from file")
            true
        } catch (e: Exception) {
            logger.error("Error loading notes from file: ${source.absolutePath}", e)
            Log.e("FileNotebook", "Load error", e)
            false
        }
    }
}