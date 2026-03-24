package com.ruslan.mynotes.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.ruslan.mynotes.data.source.remote.ApiService
import com.ruslan.mynotes.data.source.remote.AuthTokenProvider
import com.ruslan.mynotes.data.source.remote.BearerTokenProvider
import com.ruslan.mynotes.data.source.remote.RemoteNoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val API_BASE_URL = "https://hive.mrdekk.ru/todo/"
    private const val AUTH_TOKEN = "efd294b2-5a3a-457b-8eb8-a08475fa1005"

    @Provides
    @Singleton
    fun provideJsonSerializer(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $AUTH_TOKEN")
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        authInterceptor: Interceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(authInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        jsonSerializer: Json,
        httpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .client(httpClient)
        .addConverterFactory(jsonSerializer.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthTokenProvider(): AuthTokenProvider =
        BearerTokenProvider()

    @Provides
    @Singleton
    fun provideRemoteNoteSource(
        apiService: ApiService,
        tokenProvider: AuthTokenProvider
    ): RemoteNoteDataSource =
        RemoteNoteDataSource(apiService, tokenProvider)
}