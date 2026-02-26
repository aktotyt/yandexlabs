package com.ruslan.mynotes.model

import android.graphics.Color
import org.json.JSONObject
import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val text: String,
    val bgColor: Int = Color.WHITE,
    val level: Importance = Importance.NORMAL
) {
    val uid: String get() = id
    val title: String get() = name
    val content: String get() = text
    val color: Int get() = bgColor
    val importance: Importance get() = level

    companion object {
        fun fromJson(source: JSONObject): Note? {
            return try {
                val noteId = source.optString("uid").let { if (it.isNotEmpty()) it else UUID.randomUUID().toString() }
                val noteTitle = source.getString("title")
                val noteContent = source.getString("content")
                val noteColor = source.optInt("color", Color.WHITE)
                val noteLevel = when (source.optString("importance")) {
                    "HIGH" -> Importance.HIGH
                    "LOW" -> Importance.LOW
                    else -> Importance.NORMAL
                }
                Note(
                    id = noteId,
                    name = noteTitle,
                    text = noteContent,
                    bgColor = noteColor,
                    level = noteLevel
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}