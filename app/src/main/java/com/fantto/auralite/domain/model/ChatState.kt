package com.fantto.auralite.domain.model

sealed class ChatState {
    data object Loading : ChatState()
    data class Streaming(val content: String) : ChatState()
    data object Complete : ChatState()
    data class Error(val message: String) : ChatState()
}