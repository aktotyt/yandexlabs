package com.ruslan.mynotes.ui.editnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.mynotes.model.Importance
import com.ruslan.mynotes.model.Note
import com.ruslan.mynotes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            _isLoading.value = true
            _note.value = repository.getNoteById(noteId)
            _isLoading.value = false
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
            _note.value?.let { repository.saveNote(it) }
        }
    }
}