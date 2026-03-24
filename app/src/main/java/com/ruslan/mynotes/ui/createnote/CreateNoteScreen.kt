package com.ruslan.mynotes.ui.createnote

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.mynotes.ui.editnote.NoteEditContent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreateNoteScreen(
    onBack: () -> Unit,
    viewModel: CreateNoteViewModel = hiltViewModel(),
) {
    val currentNote = viewModel.note.value
    val uiEvents = viewModel.uiEvents
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        uiEvents.collectLatest { event ->
            when (event) {
                CreateNoteViewModel.UiEvent.NoteSaved -> onBack()
                is CreateNoteViewModel.UiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NoteEditContent(
            note = currentNote,
            topBarTitle = "Создать заметку",
            onTitleChange = viewModel::updateTitle,
            onContentChange = viewModel::updateContent,
            onColorChange = viewModel::updateColor,
            onImportanceChange = viewModel::updateImportance,
            onSave = {
                viewModel.saveNote()
            },
            onCancel = onBack
        )
    }
}