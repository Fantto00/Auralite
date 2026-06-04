package com.fantto.auralite.data.repository

import com.elvishew.xlog.XLog
import com.fantto.auralite.data.local.dao.ConversationDao
import com.fantto.auralite.data.local.entity.ConversationEntity
import com.fantto.auralite.data.local.entity.MessageEntity
import com.fantto.auralite.data.remote.api.LlmApiService
import com.fantto.auralite.data.remote.dto.ChatMessage
import com.fantto.auralite.data.remote.dto.LlmRequest
import com.fantto.auralite.domain.repository.ChatRepository
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID

/** ChatRepository的实现类，负责处理聊天相关的数据操作，包括与LLM API的交互和本地数据库的读写 **/
class ChatRepositoryImpl(
    private val llmApiService: LlmApiService,
    private val conversationDao: ConversationDao
) : ChatRepository {

    private val gson = Gson()

    // 发送消息并获取LLM的响应，使用Flow来处理流式数据
    override suspend fun sendMessage(
        model: String,
        messages: List<ChatMessage>
    ): Flow<String> = flow {
        val request = LlmRequest(
            model = model,
            messages = messages
        )
        XLog.d("XLog ChatRepositoryImpl：准备进入llmApiService发送请求，model=$model, 消息数 ${messages.size}")
        val response = llmApiService.streamChat(request)

        XLog.d("XLog ChatRepositoryImpl：API响应状态码=${response.code()}, isSuccessful=${response.isSuccessful}")

        if (response.isSuccessful) {
            response.body()?.byteStream()?.bufferedReader()?.use { reader ->
                var line: String?
                var chunkCount = 0
                val startTime = System.currentTimeMillis()
                XLog.d("XLog ChatRepositoryImpl：开始读取流式响应")

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    XLog.d("XLog ChatRepositoryImpl：读取到原始行=$currentLine")

                    if (currentLine.startsWith("data: ")) {
                        val data = currentLine.removePrefix("data: ").trim()
                        if (data == "[DONE]") {
                            XLog.d("XLog ChatRepositoryImpl：收到 [DONE] 信号")
                            break
                        }

                        try {
                            val jsonObject = JsonParser().parse(data).asJsonObject
                            val choices = jsonObject.getAsJsonArray("choices")
                            if (choices != null && choices.size() > 0) {
                                val delta = choices.get(0).asJsonObject.getAsJsonObject("delta")
                                if (delta != null && delta.has("content") && !delta.get("content").isJsonNull) {
                                    val content = delta.get("content").asString
                                    chunkCount++
                                    val elapsed = System.currentTimeMillis() - startTime
                                    XLog.d("XLog ChatRepositoryImpl：emit chunk #$chunkCount, 耗时=${elapsed}ms, content=$content")
                                    emit(content)
                                }
                            }
                        } catch (e: JsonSyntaxException) {
                            XLog.e("XLog ChatRepositoryImpl：json解析失败，异常：${e.message}, data=$data")
                        }
                    }
                }

                val totalTime = System.currentTimeMillis() - startTime
                XLog.d("XLog ChatRepositoryImpl：流式响应读取完成，共 $chunkCount 个chunk，总耗时=${totalTime}ms")
            }
        } else {
            XLog.e("XLog ChatRepositoryImpl：API请求失败，code=${response.code()}")
            throw Exception("LLM request failed: ${response.code()}")
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun saveConversation(title: String, messages: List<ChatMessage>) {
        val conversationId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val conversation = ConversationEntity(
            id = conversationId,
            title = title,
            createdAt = now,
            updatedAt = now
        )
        conversationDao.insertConversation(conversation)

        messages.forEachIndexed { index, message ->
            val messageEntity = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                role = message.role,
                content = message.content,
                timestamp = now + index
            )
            conversationDao.insertMessage(messageEntity)
        }
    }

    override fun getConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    override fun getMessagesByConversationId(conversationId: String): Flow<List<MessageEntity>> {
        return conversationDao.getMessagesByConversationId(conversationId)
    }

    override suspend fun getMessageCount(conversationId: String): Int {
        return conversationDao.getMessageCount(conversationId)
    }

    override suspend fun getLastMessage(conversationId: String): String? {
        return conversationDao.getLastMessage(conversationId)
    }

    override suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteMessagesByConversationId(conversationId)
        val conversation = conversationDao.getConversationById(conversationId)
        conversation?.let { conversationDao.deleteConversation(it) }
    }
}