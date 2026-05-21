package com.fantto.auralite.di

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppModule(private val context: Context) {

    private val okHttpClient : OkHttpClient by lazy {
        OkHttpClient.Builder()
            //.addInterceptor(自定义拦截器)
            .connectTimeout(30 , TimeUnit.SECONDS)
            .readTimeout(60 , TimeUnit.SECONDS)
            .writeTimeout(60 , TimeUnit.SECONDS)
            .build()
    }

    private val retrofit : Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.example.com/") // 替换为实际的 API 基础 URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}