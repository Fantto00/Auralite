package com.fantto.auralite.ui.screen.history

data class ConversationUiModel(
    val id: String,
    val title: String,
    val lastMessage: String,
    val messageCount: Int,
    val updatedAt: Long
)