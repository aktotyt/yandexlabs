package com.ruslan.mynotes.ui.editnote

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EditNoteScreen(
    noteId: String,
    onBack: () -> Unit,
    viewModel: EditNoteViewModel = hiltViewModel(),
) {
    val currentNote by viewModel.note.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    } else if (currentNote == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Заметка не найдена",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onBack) {
                    Text("Назад")
                }
            }
        }
        LaunchedEffect(Unit) {
            onBack()
        }
    } else {
        NoteEditContent(
            note = currentNote!!,
            topBarTitle = "Редактирование",
            onTitleChange = viewModel::updateTitle,
            onContentChange = viewModel::updateContent,
            onColorChange = viewModel::updateColor,
            onImportanceChange = viewModel::updateImportance,
            onSave = {
                viewModel.saveNote()
                onBack()
            },
            onCancel = onBack
        )
    }
}