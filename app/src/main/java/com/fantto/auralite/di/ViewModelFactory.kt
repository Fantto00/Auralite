package com.fantto.auralite.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fantto.auralite.ui.screen.chat.ChatViewModel
import com.fantto.auralite.ui.screen.history.HistoryViewModel
import com.fantto.auralite.ui.screen.settings.SettingsViewModel

class ViewModelFactory(
    private val appModule: AppModule
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ChatViewModel::class.java -> {
                ChatViewModel(
                    sendMessageUseCase = appModule.sendMessageUseCase,
                    synthesizeSpeechUseCase = appModule.synthesizeSpeechUseCase,
                    playAudioUseCase = appModule.playAudioUseCase,
                    settingsRepository = appModule.settingsRepository
                ) as T
            }
            SettingsViewModel::class.java -> {
                SettingsViewModel(
                    settingsRepository = appModule.settingsRepository
                ) as T
            }
            HistoryViewModel::class.java -> {
                HistoryViewModel(
                    chatRepository = appModule.chatRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}