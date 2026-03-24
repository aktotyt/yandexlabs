package com.ruslan.mynotes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GetNoteResponse(
    val status: String,
    val element: NoteDto,
    val revision: Int
)

@Serializable
data class GetNotesResponse(
    val status: String,
    val list: List<NoteDto>,
    val revision: Int
)

@Serializable
data class NoteOperationResponse(
    val status: String,
    val element: NoteDto,
    val revision: Int
)