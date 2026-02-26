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
import com.ruslan.mynotes.ui.createnote.CreateNoteScreen
import com.ruslan.mynotes.ui.editnote.EditNoteScreen
import com.ruslan.mynotes.ui.noteslist.NotesListScreen
import com.ruslan.mynotes.ui.theme.NotesTheme
import dagger.hilt.android.AndroidEntryPoint

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

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            NotesListScreen(
                onNoteClick = { noteId ->
                    navController.navigate("edit/$noteId")
                },
                onCreateNote = {
                    navController.navigate("create")
                }
            )
        }
        composable("edit/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            EditNoteScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("create") {
            CreateNoteScreen(
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
    }
}