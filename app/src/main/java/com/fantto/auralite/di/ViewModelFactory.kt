package com.fantto.auralite.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(
    private val appModule: AppModule
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            // ChatViewModel
//            com.fantto.auralite.ui.screen.chat.ChatViewModel::class.java -> {
//                com.fantto.auralite.presentation.screen.chat.ChatViewModel(
//                    chatRepository = appModule.chatRepository,
//                    audioRepository = appModule.audioRepository
//                ) as T
//            }
            // SettingsViewModel
//            com.fantto.auralite.ui.screen.settings.SettingsViewModel::class.java -> {
//                com.fantto.auralite.presentation.screen.settings.SettingsViewModel(
//                    settingsRepository = appModule.settingsRepository
//                ) as T
//            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}