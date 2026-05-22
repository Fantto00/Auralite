package com.fantto.auralite.data.remote.dto

/** TTS请求的DTO（数据传输对象）类，包含模型名称、输入文本、语音选择、响应格式和语速参数  **/
data class TtsRequest(
    val model: String,
    val input: String,
    val voice: String = "alloy",
    val response_format: String = "mp3",
    val speed: Float = 1.0f
)