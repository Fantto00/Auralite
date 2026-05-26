package com.fantto.auralite.di

import android.content.Context
import androidx.room.Room
import com.fantto.auralite.data.engine.stt.VoskEngine
import com.fantto.auralite.data.local.dao.ConversationDao
import com.fantto.auralite.data.local.database.AppDatabase
import com.fantto.auralite.data.local.datastore.SettingsDataStore
import com.fantto.auralite.data.remote.api.LlmApiService
import com.fantto.auralite.data.remote.api.TtsApiService
import com.fantto.auralite.data.remote.interceptor.AuthInterceptor
import com.fantto.auralite.data.remote.interceptor.DynamicBaseUrlInterceptor
import com.fantto.auralite.data.repository.AudioRepositoryImpl
import com.fantto.auralite.data.repository.ChatRepositoryImpl
import com.fantto.auralite.data.repository.SettingsRepositoryImpl
import com.fantto.auralite.domain.engine.SttEngine
import com.fantto.auralite.domain.repository.AudioRepository
import com.fantto.auralite.domain.repository.ChatRepository
import com.fantto.auralite.domain.repository.SettingsRepository
import com.fantto.auralite.domain.usecase.llm.SendMessageUseCase
import com.fantto.auralite.domain.usecase.tts.PlayAudioUseCase
import com.fantto.auralite.domain.usecase.tts.SynthesizeSpeechUseCase
import com.fantto.auralite.util.AudioPlayer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** 应用程序的依赖注入模块，负责创建和提供应用所需的各种组件实例 **/
class AppModule(private val context: Context) {

    companion object {
        private const val DEFAULT_BASE_URL = "https://api.openai.com/"
    }

    //本地api配置存储类的实例
    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(context)
    }

    // Room 数据库
    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "auralite_database"
        ).build()
    }

    val conversationDao: ConversationDao by lazy {
        appDatabase.conversationDao()
    }

    // OkHttp 客户端
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            //添加okhttp拦截器，动态切换baseUrl和添加认证信息
            .addInterceptor(DynamicBaseUrlInterceptor(settingsDataStore, DEFAULT_BASE_URL))
            .addInterceptor(AuthInterceptor(settingsDataStore))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // LLM Retrofit 实例
    private val llmRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // TTS Retrofit 实例
    private val ttsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API Service 实例
    val llmApiService: LlmApiService by lazy {
        llmRetrofit.create(LlmApiService::class.java)
    }

    val ttsApiService: TtsApiService by lazy {
        ttsRetrofit.create(TtsApiService::class.java)
    }

    // Repository 实例
    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(llmApiService, conversationDao)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(settingsDataStore)
    }

    // AudioPlayer 实例
    private val audioPlayer: AudioPlayer by lazy {
        AudioPlayer()
    }

    val audioRepository: AudioRepository by lazy {
        AudioRepositoryImpl(ttsApiService, audioPlayer)
    }

    // STT语音识别 Engine 实例
    val sttEngine: SttEngine by lazy {
        VoskEngine(context)
    }

    // UseCase 实例
    val sendMessageUseCase: SendMessageUseCase by lazy {
        SendMessageUseCase(chatRepository, settingsRepository)
    }

    val synthesizeSpeechUseCase: SynthesizeSpeechUseCase by lazy {
        SynthesizeSpeechUseCase(audioRepository, settingsRepository)
    }

    val playAudioUseCase: PlayAudioUseCase by lazy {
        PlayAudioUseCase(audioRepository)
    }

}