package com.fantto.auralite.data.remote.interceptor

import com.fantto.auralite.data.local.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val settingsDataStore: SettingsDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.toString()

        val apiKey = runBlocking {
            when {
                url.contains("/audio/speech") -> settingsDataStore.ttsApiKey.first()
                else -> settingsDataStore.llmApiKey.first()
            }
        }

        val request = if (apiKey.isNotEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $apiKey")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}