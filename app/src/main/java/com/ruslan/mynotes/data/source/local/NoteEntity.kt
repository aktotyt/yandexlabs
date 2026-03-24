package com.ruslan.mynotes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ruslan.mynotes.data.model.Importance
import com.ruslan.mynotes.data.model.Note
import java.util.Date

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val text: String,
    val bgColor: Int,
    val level: String,
    val createdAt: Long
) {
    fun toNote(): Note = Note(
        id = id,
        name = name,
        text = text,
        bgColor = bgColor,
        level = when (level) {
            "HIGH" -> Importance.HIGH
            "LOW" -> Importance.LOW
            else -> Importance.NORMAL
        },
        createdAt = Date(createdAt)
    )

    companion object {
        fun fromNote(note: Note): NoteEntity = NoteEntity(
            id = note.id,
            name = note.name,
            text = note.text,
            bgColor = note.bgColor,
            level = when (note.level) {
                Importance.HIGH -> "HIGH"
                Importance.LOW -> "LOW"
                else -> "NORMAL"
            },
            createdAt = note.createdAt.time
        )
    }
}