package com.ruslan.mynotes.data

import com.ruslan.mynotes.model.Note
import com.ruslan.mynotes.model.toJson
import org.json.JSONArray
import java.io.File

class FileNotebook {
    private var items = mutableListOf<Note>()
    val allNotes: List<Note>
        get() = items.toList()

    fun put(noteItem: Note) {
        items.add(noteItem)
    }

    fun delete(targetId: String): Boolean {
        return items.removeIf { it.id == targetId }
    }

    fun store(target: File): Boolean {
        return try {
            val container = JSONArray()
            items.forEach { item ->
                container.put(item.toJson())
            }
            target.writeText(container.toString())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restore(source: File): Boolean {
        return try {
            val raw = source.readText()
            val container = JSONArray(raw)
            items.clear()
            for (idx in 0 until container.length()) {
                Note.fromJson(container.getJSONObject(idx))?.let { recovered ->
                    items.add(recovered)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}