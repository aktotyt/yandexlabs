package com.ruslan.mynotes.ui.noteslist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.mynotes.ui.noteslist.components.SwipeWrapper
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNoteClick: (String) -> Unit,
    onCreateNote: () -> Unit,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiEvents = viewModel.uiEvents
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        uiEvents.collectLatest { event ->
            when (event) {
                is NotesListViewModel.UiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                NotesListViewModel.UiEvent.NoteDeleted -> {
                    snackbarHostState.showSnackbar("Заметка удалена")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Мои заметки",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.extraLarge,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Создать заметку",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                notes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Нет заметок",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onCreateNote,
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text("Создать первую заметку")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = notes,
                            key = { it.id }
                        ) { note ->
                            SwipeWrapper(
                                onDelete = { viewModel.deleteNote(note.id) },
                                content = {
                                    NotesListItem(
                                        note = note,
                                        onClick = { onNoteClick(note.id) },
                                        onDelete = { viewModel.deleteNote(note.id) }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}