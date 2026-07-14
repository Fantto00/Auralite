package com.fantto.auralite.domain.model

data class LoadedChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val timestamp: Long
)
