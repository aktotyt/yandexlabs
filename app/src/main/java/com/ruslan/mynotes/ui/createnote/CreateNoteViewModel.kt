package com.ruslan.mynotes.ui.createnote

import android.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.mynotes.model.Importance
import com.ruslan.mynotes.model.Note
import com.ruslan.mynotes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun saveNote(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveNote(_note.value)
            onSuccess()
        }
    }
}