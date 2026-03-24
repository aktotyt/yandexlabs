package com.ruslan.mynotes.data.source.remote

import com.ruslan.mynotes.data.model.GetNoteResponse
import com.ruslan.mynotes.data.model.GetNotesResponse
import com.ruslan.mynotes.data.model.NoteOperationResponse
import com.ruslan.mynotes.data.model.NoteRequest
import com.ruslan.mynotes.data.model.SyncRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("list")
    suspend fun fetchNotes(
        @Header("X-Generate-Fails") generateFailsThreshold: Int? = null,
    ): GetNotesResponse

    @GET("list/{id}")
    suspend fun fetchNote(
        @Path("id") noteId: String,
        @Header("X-Generate-Fails") generateFailsThreshold: Int? = null,
    ): GetNoteResponse

    @POST("list")
    suspend fun createNote(
        @Header("X-Last-Known-Revision") revision: Int,
        @Body request: NoteRequest,
        @Header("X-Generate-Fails") generateFailsThreshold: Int? = null,
    ): Response<NoteOperationResponse>

    @PUT("list/{id}")
    suspend fun replaceNote(
        @Header("X-Last-Known-Revision") revision: Int,
        @Path("id") noteId: String,
        @Body request: NoteRequest,
        @Header("X-Generate-Fails") generateFailsThreshold: Int? = null,
    ): Response<NoteOperationResponse>

    @PATCH("list")
    suspend fun syncNotes(
        @Header("X-Last-Known-Revision") revision: Int,
        @Body request: SyncRequest,
        @Header("X-Generate-Fails") generateFailsThreshold: Int? = null,
    ): GetNotesResponse

    @DELETE("list/{id}")
    suspend fun eraseNote(
        @Header("X-Last-Known-Revision") revision: Int,
        @Path("id") noteId: String,
        @Header("X-Generate-Fails") generateFailsThreshold: Int? = null,
    ): Response<NoteOperationResponse>
}