package com.fantto.auralite.domain.usecase.chat

import com.fantto.auralite.domain.repository.ChatRepository
import com.fantto.auralite.domain.usecase.llm.SendMessageUseCase

class SaveConversationUseCase(
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String?): String? {
        val history = sendMessageUseCase.getHistory()
        if (history.isEmpty()) return conversationId

        val title = history.firstOrNull { it.role == "user" }
            ?.content
            ?.take(50)
            ?: "新对话"

        return chatRepository.saveConversation(conversationId, title, history)
    }
}
