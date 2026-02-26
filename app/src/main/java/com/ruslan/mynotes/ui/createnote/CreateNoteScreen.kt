package com.ruslan.mynotes.ui.createnote

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.mynotes.ui.editnote.NoteEditContent

@Composable
fun CreateNoteScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    val currentNote = viewModel.note.value

    NoteEditContent(
        note = currentNote,
        topBarTitle = "Новая заметка",
        onTitleChange = viewModel::updateTitle,
        onContentChange = viewModel::updateContent,
        onColorChange = viewModel::updateColor,
        onImportanceChange = viewModel::updateImportance,
        onSave = {
            viewModel.saveNote(onSaveSuccess)
        },
        onCancel = onBack
    )
}