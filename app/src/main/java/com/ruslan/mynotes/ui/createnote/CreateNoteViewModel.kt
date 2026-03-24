package com.ruslan.mynotes.ui.createnote

import android.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.mynotes.data.model.Importance
import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val repository: NotesRepository,
) : ViewModel() {
    private val _note = mutableStateOf(
        Note(
            name = "",
            text = "",
            bgColor = Color.WHITE,
            level = Importance.NORMAL
        )
    )
    val note = _note

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    fun updateTitle(title: String) {
        _note.value = _note.value.copy(name = title)
    }

    fun updateContent(content: String) {
        _note.value = _note.value.copy(text = content)
    }

    fun updateColor(color: Int) {
        _note.value = _note.value.copy(bgColor = color)
    }

    fun updateImportance(importance: Importance) {
        _note.value = _note.value.copy(level = importance)
    }

    fun saveNote() {
        viewModelScope.launch {
            try {
                repository.storeNoteToCache(_note.value)
                repository.uploadNoteToServer(_note.value).onSuccess {
                    _uiEvents.emit(UiEvent.NoteSaved)
                }.onFailure { error ->
                    _uiEvents.emit(UiEvent.Error("Ошибка синхронизации: ${error.message}"))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.Error("Ошибка сохранения: ${e.message}"))
            }
        }
    }

    sealed class UiEvent {
        object NoteSaved : UiEvent()
        data class Error(val message: String) : UiEvent()
    }
}