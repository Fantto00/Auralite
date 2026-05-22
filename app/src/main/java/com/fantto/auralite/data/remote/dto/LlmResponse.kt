package com.fantto.auralite.data.remote.dto

/** 大模型响应的DTO（数据传输对象）类，包含响应ID、选择列表和错误信息  **/
data class LlmResponse(
    val id: String?,
    val choices: List<Choice>?,
    val error: Error?
)

data class Choice(
    val index: Int,
    val delta: Delta?,
    val message: ChatMessage?
)

data class Delta(
    val content: String?
)

data class Error(
    val message: String,
    val type: String?
)