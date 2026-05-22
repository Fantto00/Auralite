package com.fantto.auralite.data.repository

import com.fantto.auralite.data.local.datastore.SettingsDataStore
import com.fantto.auralite.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/** 设置相关的仓库实现类，负责从数据源获取和保存设置数据 **/
class SettingsRepositoryImpl(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val llmBaseUrl: Flow<String> = settingsDataStore.llmBaseUrl
    override val llmApiKey: Flow<String> = settingsDataStore.llmApiKey
    override val llmModel: Flow<String> = settingsDataStore.llmModel

    override val ttsApiKey: Flow<String> = settingsDataStore.ttsApiKey
    override val ttsVoice: Flow<String> = settingsDataStore.ttsVoice
    override val ttsSpeed: Flow<Float> = settingsDataStore.ttsSpeed

    override val sttLanguage: Flow<String> = settingsDataStore.sttLanguage

    override suspend fun saveLlmConfig(baseUrl: String, apiKey: String, model: String) {
        settingsDataStore.saveLlmConfig(baseUrl, apiKey, model)
    }

    override suspend fun saveTtsConfig(apiKey: String, voice: String, speed: Float) {
        settingsDataStore.saveTtsConfig(apiKey, voice, speed)
    }

    override suspend fun saveSttLanguage(language: String) {
        settingsDataStore.saveSttLanguage(language)
    }
}