package com.fantto.auralite.ui.screen.chat

data class MessageUiModel(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val isStreaming: Boolean = false
)