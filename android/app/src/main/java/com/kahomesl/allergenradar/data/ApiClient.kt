package com.kahomesl.allergenradar.data

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

object ApiClient {
    fun create(baseUrl: String, debug: Boolean = false): PollenApi {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(baseUrl.ensureTrailingSlash())
            .client(createHttpClient(debug))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PollenApi::class.java)
    }

    internal fun createHttpClient(debug: Boolean): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (debug) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .build()
    }

    private fun String.ensureTrailingSlash(): String = if (endsWith('/')) this else "$this/"
}
