package com.ruslan.mynotes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    @SerialName("list") val notes: List<NoteDto>
)

@Serializable
data class NoteRequest(
    @SerialName("element") val note: NoteDto
)