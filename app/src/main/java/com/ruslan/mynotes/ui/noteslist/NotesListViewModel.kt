package com.ruslan.mynotes.ui.noteslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: NotesRepository,
) : ViewModel() {
    val notes: StateFlow<List<Note>> = repository.observeAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.fetchAllNotes(forceRefresh = false)
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.Error("Ошибка загрузки: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteNoteOnServer(noteId).fold(
                    onSuccess = {
                        _uiEvents.emit(UiEvent.NoteDeleted)
                        repository.removeNoteFromCache(noteId)
                    },
                    onFailure = { error ->
                        _uiEvents.emit(UiEvent.Error("Ошибка удаления: ${error.message}"))
                    }
                )
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.Error("Ошибка удаления: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class UiEvent {
        object NoteDeleted : UiEvent()
        data class Error(val message: String) : UiEvent()
    }
}