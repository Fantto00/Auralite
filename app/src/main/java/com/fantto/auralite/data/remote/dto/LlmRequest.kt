package com.fantto.auralite.data.remote.dto

/** 大模型请求的DTO（数据传输对象）类，包含模型名称、消息列表、是否流式响应和温度参数  **/
data class LlmRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
    val temperature: Float = 0.7f
)

data class ChatMessage(
    val role: String,
    val content: String
)