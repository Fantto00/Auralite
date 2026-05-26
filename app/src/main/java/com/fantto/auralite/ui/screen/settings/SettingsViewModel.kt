package com.fantto.auralite.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import com.fantto.auralite.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _llmBaseUrl = MutableStateFlow("")
    val llmBaseUrl: StateFlow<String> = _llmBaseUrl.asStateFlow()

    private val _llmApiKey = MutableStateFlow("")
    val llmApiKey: StateFlow<String> = _llmApiKey.asStateFlow()

    private val _llmModel = MutableStateFlow("")
    val llmModel: StateFlow<String> = _llmModel.asStateFlow()

    private val _ttsApiKey = MutableStateFlow("")
    val ttsApiKey: StateFlow<String> = _ttsApiKey.asStateFlow()

    private val _ttsModel = MutableStateFlow("")
    val ttsModel: StateFlow<String> = _ttsModel.asStateFlow()

    private val _ttsVoice = MutableStateFlow("alloy")
    val ttsVoice: StateFlow<String> = _ttsVoice.asStateFlow()

    private val _ttsSpeed = MutableStateFlow(1.0f)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()

    private val _sttLanguage = MutableStateFlow("zh")
    val sttLanguage: StateFlow<String> = _sttLanguage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            _llmBaseUrl.value = settingsRepository.llmBaseUrl.first()
            _llmApiKey.value = settingsRepository.llmApiKey.first()
            _llmModel.value = settingsRepository.llmModel.first()
            _ttsApiKey.value = settingsRepository.ttsApiKey.first()
            _ttsModel.value = settingsRepository.ttsModel.first()
            _ttsVoice.value = settingsRepository.ttsVoice.first()
            _ttsSpeed.value = settingsRepository.ttsSpeed.first()
            _sttLanguage.value = settingsRepository.sttLanguage.first()
            XLog.d("XLog SettingsViewModel：配置加载完成")
        }
    }

    fun updateLlmBaseUrl(url: String) {
        _llmBaseUrl.value = url
    }

    fun updateLlmApiKey(key: String) {
        _llmApiKey.value = key
    }

    fun updateLlmModel(model: String) {
        _llmModel.value = model
    }

    fun updateTtsApiKey(key: String) {
        _ttsApiKey.value = key
    }

    fun updateTtsModel(model: String) {
        _ttsModel.value = model
    }

    fun updateTtsVoice(voice: String) {
        _ttsVoice.value = voice
    }

    fun updateTtsSpeed(speed: Float) {
        _ttsSpeed.value = speed
    }

    fun updateSttLanguage(language: String) {
        _sttLanguage.value = language
    }

    fun saveAllConfig() {
        viewModelScope.launch {
            try {
                settingsRepository.saveLlmConfig(
                    baseUrl = _llmBaseUrl.value,
                    apiKey = _llmApiKey.value,
                    model = _llmModel.value
                )
                settingsRepository.saveTtsConfig(
                    apiKey = _ttsApiKey.value,
                    model = _ttsModel.value,
                    voice = _ttsVoice.value,
                    speed = _ttsSpeed.value
                )
                settingsRepository.saveSttLanguage(_sttLanguage.value)
                _saveSuccess.value = true
                XLog.d("XLog SettingsViewModel：配置保存成功")
            } catch (e: Exception) {
                XLog.e("XLog SettingsViewModel：配置保存失败 ${e.message}")
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}