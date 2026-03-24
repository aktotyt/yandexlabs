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
        const val OAUTH_CLIENT_ID = "0d0970774e284fa8ba9ff70b6b06479a"
    }

    private var currentRevision: Int = 0
    private val isRefreshingToken = AtomicBoolean(false)

    suspend fun init() {
        loadNotes()
    }

    suspend fun loadNotes(): List<Note> {
        return executeWithRetry {
            val response = apiService.fetchNotes()
            currentRevision = response.revision
            response.list.map { dto -> dto.toNote() }
        }
    }

    suspend fun sendNote(note: Note): Note {
        return executeWithRetry(isWriteOperation = true) {
            try {
                val response = apiService.createNote(
                    revision = currentRevision,
                    request = NoteRequest(note.toDto())
                ).also { validateResponse(it) }

                currentRevision = response.body()?.revision ?: currentRevision
                response.body()!!.element.toNote()
            } catch (e: IllegalStateException) {
                if (e.message == "Data is out of sync") {
                    loadNotes()
                    val retryResponse = apiService.createNote(
                        revision = currentRevision,
                        request = NoteRequest(note.toDto())
                    ).also { validateResponse(it) }

                    currentRevision = retryResponse.body()?.revision ?: currentRevision
                    retryResponse.body()!!.element.toNote()
                } else {
                    throw e
                }
            }
        }
    }

    suspend fun modifyNote(note: Note): Note {
        return executeWithRetry(isWriteOperation = true) {
            try {
                val response = apiService.replaceNote(
                    revision = currentRevision,
                    noteId = note.id,
                    request = NoteRequest(note.toDto())
                ).also { validateResponse(it) }

                currentRevision = response.body()?.revision ?: currentRevision
                response.body()!!.element.toNote()
            } catch (e: IllegalStateException) {
                if (e.message == "Data is out of sync") {
                    loadNotes()
                    val retryResponse = apiService.replaceNote(
                        revision = currentRevision,
                        noteId = note.id,
                        request = NoteRequest(note.toDto())
                    ).also { validateResponse(it) }

                    currentRevision = retryResponse.body()?.revision ?: currentRevision
                    retryResponse.body()!!.element.toNote()
                } else {
                    throw e
                }
            }
        }
    }

    suspend fun removeNote(id: String): Boolean {
        return executeWithRetry(isWriteOperation = true) {
            try {
                val response = apiService.eraseNote(
                    revision = currentRevision,
                    noteId = id
                ).also { validateResponse(it) }

                currentRevision = response.body()?.revision ?: currentRevision
                true
            } catch (e: IllegalStateException) {
                if (e.message == "Data is out of sync") {
                    loadNotes()
                    val retryResponse = apiService.eraseNote(
                        revision = currentRevision,
                        noteId = id
                    ).also { validateResponse(it) }

                    currentRevision = retryResponse.body()?.revision ?: currentRevision
                    true
                } else {
                    throw e
                }
            }
        }
    }

    suspend fun syncNotes(notes: List<Note>): List<Note> {
        return executeWithRetry(isWriteOperation = true) {
            try {
                val response = apiService.syncNotes(
                    revision = currentRevision,
                    request = SyncRequest(notes.map { it.toDto() })
                )
                currentRevision = response.revision
                response.list.map { it.toNote() }
            } catch (e: IllegalStateException) {
                if (e.message == "Data is out of sync") {
                    loadNotes()
                    val retryResponse = apiService.syncNotes(
                        revision = currentRevision,
                        request = SyncRequest(notes.map { it.toDto() })
                    )
                    currentRevision = retryResponse.revision
                    retryResponse.list.map { it.toNote() }
                } else {
                    throw e
                }
            }
        }
    }

    private fun validateResponse(response: retrofit2.Response<*>) {
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw when {
                errorBody?.contains("unsynchronized data") == true -> {
                    IllegalStateException("Data is out of sync")
                }
                else -> HttpException(response)
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
        createdAt = this.createdAt.time,
        changedAt = Date().time,
        lastUpdatedBy = "android-client",
        color = this.bgColor.takeIf { it != Color.WHITE }?.let {
            String.format("#%06X", 0xFFFFFF and it)
        }
    )

    private fun NoteDto.toNote(): Note {
        val title: String
        val content: String

        if (text.contains('\n')) {
            val splitIndex = text.indexOf('\n')
            title = text.substring(0, splitIndex)
            content = text.substring(splitIndex + 1)
        } else {
            title = text
            content = ""
        }

        return Note(
            id = id,
            name = title,
            text = content,
            bgColor = color?.let { Color.parseColor(it) } ?: Color.WHITE,
            level = when (importance.lowercase()) {
                "important" -> Importance.HIGH
                "low" -> Importance.LOW
                else -> Importance.NORMAL
            },
            createdAt = Date(createdAt)
        )
    }
}