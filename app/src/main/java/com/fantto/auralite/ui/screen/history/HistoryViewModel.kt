package com.fantto.auralite.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import android.database.sqlite.SQLiteException
import com.fantto.auralite.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationUiModel>>(emptyList())
    val conversations: StateFlow<List<ConversationUiModel>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.getConversations().collect { conversationList ->
                val uiModels = conversationList.map { conversation ->
                    val messageCount = chatRepository.getMessageCount(conversation.id)
                    val lastMessage = chatRepository.getLastMessage(conversation.id) ?: ""
                    ConversationUiModel(
                        id = conversation.id,
                        title = conversation.title,
                        lastMessage = lastMessage,
                        messageCount = messageCount,
                        updatedAt = conversation.updatedAt
                    )
                }
                _conversations.value = uiModels
                _isLoading.value = false
                XLog.d("XLog HistoryViewModel：加载了 ${_conversations.value.size} 个对话")
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteConversation(id)
                XLog.d("XLog HistoryViewModel：删除对话 $id")
            } catch (e: SQLiteException) {
                XLog.e("XLog HistoryViewModel：删除失败 ${e.message}")
            }
        }
    }
}