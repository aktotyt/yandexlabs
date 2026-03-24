package com.ruslan.mynotes.ui.editnote

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EditNoteScreen(
    noteId: String,
    onBack: () -> Unit,
    viewModel: EditNoteViewModel = hiltViewModel(),
) {
    val note by viewModel.note.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiEvents = viewModel.uiEvents
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        uiEvents.collectLatest { event ->
            when (event) {
                EditNoteViewModel.UiEvent.NoteSaved -> onBack()
                EditNoteViewModel.UiEvent.NoteDeleted -> onBack()
                is EditNoteViewModel.UiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            note == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Заметка не найдена")
                }
                LaunchedEffect(Unit) {
                    onBack()
                }
            }
            else -> {
                NoteEditContent(
                    note = note!!,
                    topBarTitle = "Редактировать",
                    onTitleChange = viewModel::updateTitle,
                    onContentChange = viewModel::updateContent,
                    onColorChange = viewModel::updateColor,
                    onImportanceChange = viewModel::updateImportance,
                    onSave = viewModel::saveNote,
                    onCancel = onBack
                )
            }
        }
    }
}