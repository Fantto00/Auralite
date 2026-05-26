package com.fantto.auralite.domain.usecase.llm

import com.elvishew.xlog.XLog
import com.fantto.auralite.data.remote.dto.ChatMessage
import com.fantto.auralite.domain.model.ChatState
import com.fantto.auralite.domain.repository.ChatRepository
import com.fantto.auralite.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/** 发送消息的用例类，负责处理用户输入的消息，调用ChatRepository发送消息，并维护对话历史记录 **/
class SendMessageUseCase(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository
) {
    private val conversationHistory = mutableListOf<ChatMessage>()

    // 发送消息，接受用户输入的消息，返回一个Flow来表示聊天状态的变化
    operator fun invoke(message: String): Flow<ChatState> = flow {
        emit(ChatState.Loading)

        conversationHistory.add(ChatMessage(role = "user", content = message))

        val model = settingsRepository.llmModel.first()
        val messages = conversationHistory.toList()
        XLog.d("XLog SendMessageUseCase：发送消息 model=$model, 消息数 ${messages.size}")

        var fullResponse = ""

        //将消息发给大模型 分块返回
        chatRepository.sendMessage(model, messages)
            .collect { chunk ->
                fullResponse += chunk
                emit(ChatState.Streaming(fullResponse))
            }

        conversationHistory.add(ChatMessage(role = "assistant", content = fullResponse))

        emit(ChatState.Complete)
        XLog.d("XLog SendMessageUseCase：对话完成，历史消息数 ${conversationHistory.size}")
    }.catch { e ->
        XLog.e("XLog SendMessageUseCase：发送失败 ${e.message}")
        emit(ChatState.Error(e.message ?: "Unknown error"))
    }

    fun clearHistory() {
        conversationHistory.clear()
        XLog.d("XLog SendMessageUseCase：清空历史记录")
    }

    fun getHistory(): List<ChatMessage> = conversationHistory.toList()
}