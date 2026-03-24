package com.ruslan.mynotes.data.source.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BearerTokenProvider @Inject constructor() : AuthTokenProvider {
    override fun refreshToken(clientId: String): String {
        return "efd294b2-5a3a-457b-8eb8-a08475fa1005"
    }
}