package com.ruslan.mynotes.data.source.remote

import android.graphics.Color
import com.ruslan.mynotes.data.model.Importance
import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.model.NoteDto
import com.ruslan.mynotes.data.model.NoteRequest
import com.ruslan.mynotes.data.model.SyncRequest
import kotlinx.coroutines.delay
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteNoteDataSource @Inject constructor(
    private val apiService: ApiService,
    private val tokenProvider: AuthTokenProvider
) : Authenticator {
    private companion object {
        const val MAX_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 1000L
        const val INITIAL_VERSION = 0
        const val OAUTH_CLIENT_ID = "0d0970774e284fa8ba9ff70b6b06479a"
    }

    private var currentRevision: Int = INITIAL_VERSION
    private val isRefreshingToken = AtomicBoolean(false)

    suspend fun loadNotes(): List<Note> {
        return executeWithRetry {
            val response = apiService.fetchNotes()
            currentRevision = response.revision
            response.list.map { dto -> dto.toNote() }
        }
    }

    suspend fun sendNote(note: Note): Note {
        return executeWithRetry(isWriteOperation = true) {
            val response = apiService.createNote(
                revision = currentRevision,
                request = NoteRequest(note.toDto())
            ).also { validateResponse(it) }

            currentRevision = response.body()?.revision ?: currentRevision
            response.body()!!.element.toNote()
        }
    }

    suspend fun modifyNote(note: Note): Note {
        return executeWithRetry(isWriteOperation = true) {
            val response = apiService.replaceNote(
                revision = currentRevision,
                noteId = note.id,
                request = NoteRequest(note.toDto())
            ).also { validateResponse(it) }

            currentRevision = response.body()?.revision ?: currentRevision
            response.body()!!.element.toNote()
        }
    }

    suspend fun removeNote(id: String): Boolean {
        return executeWithRetry(isWriteOperation = true) {
            val response = apiService.eraseNote(
                revision = currentRevision,
                noteId = id
            ).also { validateResponse(it) }

            currentRevision = response.body()?.revision ?: currentRevision
            true
        }
    }

    suspend fun syncNotes(notes: List<Note>): List<Note> {
        return executeWithRetry(isWriteOperation = true) {
            val response = apiService.syncNotes(
                revision = currentRevision,
                request = SyncRequest(notes.map { it.toDto() })
            )
            currentRevision = response.revision
            response.list.map { it.toNote() }
        }
    }

    private fun validateResponse(response: retrofit2.Response<*>) {
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw HttpException(response).also {
                if (errorBody?.contains("unsynchronized data") == true) {
                    throw IllegalStateException("Local data is out of sync with server")
                }
            }
        }
    }

    private suspend fun <T> executeWithRetry(
        isWriteOperation: Boolean = false,
        block: suspend () -> T
    ): T {
        var lastError: Exception? = null
        var attempt = 0

        while (attempt < MAX_ATTEMPTS) {
            try {
                return block()
            } catch (e: Exception) {
                lastError = e
                if (shouldRetry(e, isWriteOperation)) {
                    if (e is IllegalStateException && e.message == "Local data is out of sync with server") {
                        loadNotes()
                    }
                    delay(RETRY_DELAY_MS * (attempt + 1))
                    attempt++
                } else {
                    throw e
                }
            }
        }
        throw lastError ?: IllegalStateException("Unknown error occurred")
    }

    private fun shouldRetry(e: Exception, isWriteOperation: Boolean): Boolean {
        return when (e) {
            is HttpException -> e.code() in 500..599 || e.code() == 401
            is SocketTimeoutException -> true
            is IllegalStateException -> true
            else -> false
        }
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (isRefreshingToken.getAndSet(true)) return null

        return try {
            val newToken = tokenProvider.refreshToken(OAUTH_CLIENT_ID)
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } finally {
            isRefreshingToken.set(false)
        }
    }

    private fun Note.toDto(): NoteDto = NoteDto(
        id = this.id,
        text = "${this.name}\n${this.text}",
        importance = when (this.level) {
            Importance.HIGH -> "important"
            Importance.LOW -> "low"
            else -> "basic"
        },
        done = false,
        createdAt = System.currentTimeMillis(),
        color = this.bgColor.takeIf { it != Color.WHITE }?.let {
            String.format("#%06X", 0xFFFFFF and it)
        }
    )

    private fun NoteDto.toNote(): Note {
        val parts = this.text.split("\n", limit = 2)
        val title = parts.getOrNull(0) ?: ""
        val content = parts.getOrNull(1) ?: ""

        return Note(
            id = this.id,
            name = title,
            text = content,
            bgColor = this.color?.let { Color.parseColor(it) } ?: Color.WHITE,
            level = when (this.importance.lowercase()) {
                "important" -> Importance.HIGH
                "low" -> Importance.LOW
                else -> Importance.NORMAL
            }
        )
    }
}