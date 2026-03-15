package com.ruslan.mynotes.data.source.remote

import android.util.Log
import com.ruslan.mynotes.data.model.Note
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteNoteDataSource @Inject constructor() {
    private val networkDelay = 1000L

    suspend fun loadNotes(): List<Note> {
        delay(networkDelay)
        Log.d("RemoteNoteDataSource", "Fetching notes from server (stub) - returning empty list")
        return emptyList()
    }

    suspend fun sendNote(note: Note) {
        delay(networkDelay)
        Log.d("RemoteNoteDataSource", "Sending note to server (stub): ${note.name} (ID: ${note.id})")
    }

    suspend fun removeNote(id: String) {
        delay(networkDelay)
        Log.d("RemoteNoteDataSource", "Deleting note on server (stub): ID $id")
    }
}