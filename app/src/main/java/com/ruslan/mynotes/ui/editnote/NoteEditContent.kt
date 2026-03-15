package com.ruslan.mynotes.ui.editnote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ruslan.mynotes.data.model.Importance
import com.ruslan.mynotes.data.model.Note
import com.ruslan.mynotes.ui.editnote.components.ColorSelector
import com.ruslan.mynotes.ui.editnote.components.ImportanceSelector
import com.ruslan.mynotes.ui.noteslist.components.NoteEditTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditContent(
    note: Note,
    topBarTitle: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onColorChange: (Int) -> Unit,
    onImportanceChange: (Importance) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            NoteEditTopBar(
                title = topBarTitle,
                onSave = onSave,
                onCancel = onCancel
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = note.title,
                onValueChange = onTitleChange,
                label = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )

            OutlinedTextField(
                value = note.content,
                onValueChange = onContentChange,
                label = { Text("Содержимое") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                minLines = 6,
                maxLines = 12
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ColorSelector(
                selectedColor = note.color,
                onColorPicked = onColorChange
            )

            ImportanceSelector(
                currentLevel = note.importance,
                onLevelSelected = onImportanceChange
            )
        }
    }
}