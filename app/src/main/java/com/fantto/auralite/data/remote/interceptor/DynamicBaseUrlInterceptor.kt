package com.fantto.auralite.data.remote.interceptor

import com.elvishew.xlog.XLog
import com.fantto.auralite.data.local.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/** OkHttp 拦截器，用于动态切换 API 请求的 Base URL **/
class DynamicBaseUrlInterceptor(
    private val settingsDataStore: SettingsDataStore,
    private val defaultBaseUrl: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.toString()

        val baseUrl = runBlocking {
            when {
                url.contains("/audio/speech") -> {
                    settingsDataStore.llmBaseUrl.first().ifEmpty { defaultBaseUrl }
                }
                else -> {
                    settingsDataStore.llmBaseUrl.first().ifEmpty { defaultBaseUrl }
                }
            }
        }

        val newUrl = if (baseUrl.isNotEmpty()) {
            val httpUrl = baseUrl.toHttpUrl()
            original.url.newBuilder()
                .scheme(httpUrl.scheme)
                .host(httpUrl.host)
                .port(httpUrl.port)
                .build()
        } else {
            original.url
        }

        val newRequest = original.newBuilder()
            .url(newUrl)
            .build()

        XLog.d("XLog DynamicBaseUrlInterceptor：请求URL ${newRequest.url}")

        return chain.proceed(newRequest)
    }
}