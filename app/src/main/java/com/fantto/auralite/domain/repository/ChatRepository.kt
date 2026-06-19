package com.fantto.auralite.domain.repository

import com.fantto.auralite.data.local.entity.ConversationEntity
import com.fantto.auralite.data.local.entity.MessageEntity
import com.fantto.auralite.data.remote.dto.ChatMessage
import kotlinx.coroutines.flow.Flow

/** 聊天相关的仓库接口，定义了发送消息、保存对话、获取对话列表和消息列表以及删除对话的功能 **/
interface ChatRepository {

    suspend fun sendMessage(
        model: String,
        messages: List<ChatMessage>
    ): Flow<String>

    suspend fun saveConversation(conversationId: String?, title: String, messages: List<ChatMessage>)

    suspend fun getLastConversationId(): String?

    fun getConversations(): Flow<List<ConversationEntity>>

    fun getMessagesByConversationId(conversationId: String): Flow<List<MessageEntity>>

    suspend fun getMessageCount(conversationId: String): Int

    suspend fun getLastMessage(conversationId: String): String?

    suspend fun deleteConversation(conversationId: String)
}