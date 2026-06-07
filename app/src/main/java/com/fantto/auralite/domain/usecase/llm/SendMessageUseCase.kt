package com.fantto.auralite.domain.usecase.llm

import com.elvishew.xlog.XLog
import com.fantto.auralite.data.remote.dto.ChatMessage
import com.fantto.auralite.data.repository.OfflineMessageQueue
import com.fantto.auralite.domain.model.ChatState
import com.fantto.auralite.domain.repository.ChatRepository
import com.fantto.auralite.domain.repository.SettingsRepository
import com.fantto.auralite.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class SendMessageUseCase(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository,
    private val networkMonitor: NetworkMonitor,
    private val offlineQueue: OfflineMessageQueue
) {
    private val conversationHistory = mutableListOf<ChatMessage>()

    // 发送消息，接受用户输入的消息，返回一个Flow来表示聊天状态的变化
    operator fun invoke(message: String): Flow<ChatState> = flow {
        emit(ChatState.Loading)

        conversationHistory.add(ChatMessage(role = "user", content = message))

        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) {
            conversationHistory.removeAt(conversationHistory.lastIndex)
            offlineQueue.enqueue(message)
            XLog.d("XLog SendMessageUseCase：网络离线，消息已加入队列")
            emit(ChatState.Pending)
            return@flow
        }

        val model = settingsRepository.llmModel.first()
        val messages = conversationHistory.toList()
        XLog.d("XLog SendMessageUseCase：发送消息 model=$model, 消息数 ${messages.size}")

        var fullResponse = ""
        var chunkCount = 0

        chatRepository.sendMessage(model, messages)
            .collect { chunk ->
                chunkCount++
                fullResponse += chunk
                XLog.d("XLog SendMessageUseCase：收到chunk #$chunkCount, 当前总长度=${fullResponse.length}")
                emit(ChatState.Streaming(fullResponse))
            }

        XLog.d("XLog SendMessageUseCase：流式接收完成，共 $chunkCount 个chunk，总长度=${fullResponse.length}")

        conversationHistory.add(ChatMessage(role = "assistant", content = fullResponse))

        emit(ChatState.Complete)
        XLog.d("XLog SendMessageUseCase：对话完成，历史消息数 ${conversationHistory.size}")
    }.catch { e ->
        XLog.e("XLog SendMessageUseCase：发送失败 ${e.message}")
        emit(ChatState.Error(e.message ?: "Unknown error"))
    }

    fun flushPendingMessages(): Flow<ChatState> = flow<ChatState> {
        val pendingMessages = offlineQueue.peekAll()
        if (pendingMessages.isEmpty()) {
            emit(ChatState.Complete)
            return@flow
        }

        XLog.d("XLog SendMessageUseCase：发送 ${pendingMessages.size} 条待发送消息")
        for (msg in pendingMessages) {
            conversationHistory.add(ChatMessage(role = "user", content = msg.content))
            emit(ChatState.Loading)

            val model = settingsRepository.llmModel.first()
            val messages = conversationHistory.toList()
            var fullResponse = ""

            chatRepository.sendMessage(model, messages).collect { chunk ->
                fullResponse += chunk
                emit(ChatState.Streaming(fullResponse))
            }

            conversationHistory.add(ChatMessage(role = "assistant", content = fullResponse))
            offlineQueue.remove(msg.id)
            XLog.d("XLog SendMessageUseCase：离线消息已发送，id=${msg.id}")
        }

        emit(ChatState.Complete)
    }.catch { e ->
        XLog.e("XLog SendMessageUseCase：发送离线消息失败 ${e.message}")
        emit(ChatState.Error(e.message ?: "Unknown error"))
    }

    fun clearHistory() {
        conversationHistory.clear()
        XLog.d("XLog SendMessageUseCase：清空历史记录")
    }

    fun getHistory(): List<ChatMessage> = conversationHistory.toList()

    fun setHistory(messages: List<ChatMessage>) {
        conversationHistory.clear()
        conversationHistory.addAll(messages)
        XLog.d("XLog SendMessageUseCase：设置历史记录，消息数 ${messages.size}")
    }

    suspend fun hasPendingMessages(): Boolean {
        return offlineQueue.size() > 0
    }

    suspend fun clearOfflineQueue() {
        offlineQueue.clear()
    }
}