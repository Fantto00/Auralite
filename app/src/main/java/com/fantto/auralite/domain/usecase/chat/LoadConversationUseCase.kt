package com.fantto.auralite.domain.usecase.chat

import com.fantto.auralite.data.remote.dto.ChatMessage
import com.fantto.auralite.domain.model.LoadedChatMessage
import com.fantto.auralite.domain.repository.ChatRepository
import com.fantto.auralite.domain.usecase.llm.SendMessageUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class LoadConversationUseCase(
    private val chatRepository: ChatRepository,
    private val sendMessageUseCase: SendMessageUseCase
) {
    operator fun invoke(conversationId: String): Flow<List<LoadedChatMessage>> {
        return chatRepository.getMessagesByConversationId(conversationId)
            .onStart { sendMessageUseCase.clearHistory() }
            .map { entities ->
                sendMessageUseCase.setHistory(
                    entities.map { ChatMessage(role = it.role, content = it.content) }
                )
                entities.map {
                    LoadedChatMessage(
                        id = it.id,
                        role = it.role,
                        content = it.content,
                        timestamp = it.timestamp
                    )
                }
            }
    }
}
