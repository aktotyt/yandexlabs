package com.ruslan.mynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ruslan.mynotes.ui.createnote.CreateNoteScreen
import com.ruslan.mynotes.ui.editnote.EditNoteScreen
import com.ruslan.mynotes.ui.noteslist.NotesListScreen
import com.ruslan.mynotes.ui.theme.NotesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotesTheme {
                AppNavigation()
            }
        }
    }
}

@Serializable
object NotesList

@Serializable
data class EditNote(val noteId: String)

@Serializable
object CreateNote

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = NotesList) {
        composable<NotesList> {
            NotesListScreen(
                onNoteClick = { noteId ->
                    navController.navigate(EditNote(noteId))
                },
                onCreateNote = {
                    navController.navigate(CreateNote)
                }
            )
        }

        composable<EditNote> { backStackEntry ->
            val note: EditNote = backStackEntry.toRoute()
            EditNoteScreen(
                noteId = note.noteId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<CreateNote> {
            CreateNoteScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}