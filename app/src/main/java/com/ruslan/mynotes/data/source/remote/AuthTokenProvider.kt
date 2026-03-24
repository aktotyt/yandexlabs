package com.ruslan.mynotes.data.source.remote

interface AuthTokenProvider {
    fun refreshToken(clientId: String): String
}