package com.ruslan.mynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ruslan.mynotes.ui.screens.NotesApp
import com.ruslan.mynotes.ui.theme.SpaceNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpaceNotesTheme {
                NotesApp()
            }
        }
    }
}