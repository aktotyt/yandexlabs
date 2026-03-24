package com.ruslan.mynotes.ui.editnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.mynotes.data.model.Importance
import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val repository: NotesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var localNote = repository.observeNoteById(noteId).first()

                if (localNote == null) {
                    repository.syncNotesFromServer()
                    localNote = repository.observeNoteById(noteId).first()
                }

                _note.value = localNote
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.Error("Ошибка загрузки: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(title: String) {
        _note.value = _note.value?.copy(name = title)
    }

    fun updateContent(content: String) {
        _note.value = _note.value?.copy(text = content)
    }

    fun updateColor(color: Int) {
        _note.value = _note.value?.copy(bgColor = color)
    }

    fun updateImportance(importance: Importance) {
        _note.value = _note.value?.copy(level = importance)
    }

    fun saveNote() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentNote = _note.value ?: return@launch
                repository.storeNoteToCache(currentNote)

                repository.uploadNoteToServer(currentNote).onSuccess {
                    _uiEvents.emit(UiEvent.NoteSaved)
                }.onFailure { error ->
                    _uiEvents.emit(UiEvent.Error("Ошибка синхронизации: ${error.message}"))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.Error("Ошибка сохранения: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteNoteOnServer(noteId).onSuccess {
                    _uiEvents.emit(UiEvent.NoteDeleted)
                    repository.removeNoteFromCache(noteId)
                }.onFailure { error ->
                    _uiEvents.emit(UiEvent.Error("Ошибка удаления: ${error.message}"))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.Error("Ошибка удаления: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class UiEvent {
        object NoteSaved : UiEvent()
        object NoteDeleted : UiEvent()
        data class Error(val message: String) : UiEvent()
    }
}