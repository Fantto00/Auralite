package com.fantto.auralite.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val LLM_BASE_URL = stringPreferencesKey("llm_base_url")
        private val LLM_API_KEY = stringPreferencesKey("llm_api_key")
        private val LLM_MODEL = stringPreferencesKey("llm_model")

        private val TTS_API_KEY = stringPreferencesKey("tts_api_key")
        private val TTS_VOICE = stringPreferencesKey("tts_voice")
        private val TTS_SPEED = floatPreferencesKey("tts_speed")

        private val STT_LANGUAGE = stringPreferencesKey("stt_language")
    }

    // 通过flow暴露数据监听api配置的变化
    val llmBaseUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LLM_BASE_URL] ?: ""
    }

    val llmApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LLM_API_KEY] ?: ""
    }

    val llmModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LLM_MODEL] ?: ""
    }

    val ttsApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TTS_API_KEY] ?: ""
    }

    val ttsVoice: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TTS_VOICE] ?: "alloy"
    }

    val ttsSpeed: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TTS_SPEED] ?: 1.0f
    }

    val sttLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[STT_LANGUAGE] ?: "zh"
    }


    // 通过save保存api配置到本地
    suspend fun saveLlmConfig(baseUrl: String, apiKey: String, model: String) {
        context.dataStore.edit { preferences ->
            preferences[LLM_BASE_URL] = baseUrl
            preferences[LLM_API_KEY] = apiKey
            preferences[LLM_MODEL] = model
        }
    }

    suspend fun saveTtsConfig(apiKey: String, voice: String, speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[TTS_API_KEY] = apiKey
            preferences[TTS_VOICE] = voice
            preferences[TTS_SPEED] = speed
        }
    }

    suspend fun saveSttLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[STT_LANGUAGE] = language
        }
    }
}