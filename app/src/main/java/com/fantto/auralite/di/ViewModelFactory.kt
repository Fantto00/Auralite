package com.fantto.auralite.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fantto.auralite.ui.screen.chat.ChatViewModel

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
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}