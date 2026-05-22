package com.fantto.auralite.domain.repository

import kotlinx.coroutines.flow.Flow

/** 设置相关的仓库接口，定义了获取和保存LLM、TTS和STT配置的功能 **/
interface SettingsRepository {

    val llmBaseUrl: Flow<String>
    val llmApiKey: Flow<String>
    val llmModel: Flow<String>

    val ttsApiKey: Flow<String>
    val ttsVoice: Flow<String>
    val ttsSpeed: Flow<Float>

    val sttLanguage: Flow<String>

    suspend fun saveLlmConfig(baseUrl: String, apiKey: String, model: String)
    suspend fun saveTtsConfig(apiKey: String, voice: String, speed: Float)
    suspend fun saveSttLanguage(language: String)
}