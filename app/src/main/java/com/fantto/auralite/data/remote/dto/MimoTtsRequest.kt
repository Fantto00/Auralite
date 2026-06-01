package com.fantto.auralite.data.remote.dto

/** MiMo TTS 请求的DTO（数据传输对象）类，包含模型名称、消息列表和音频配置  **/
data class MimoTtsRequest(
    val model: String,
    val messages: List<TtsMessage>,
    val audio: TtsAudioConfig,
    val stream: Boolean = false
)

/** TTS 消息类，role 为 "user"（可选，用于风格控制）或 "assistant"（必填，合成文本）  **/
data class TtsMessage(
    val role: String,
    val content: String
)

/** TTS 音频配置类，format 支持 "wav"（非流式）或 "pcm16"（流式），voice 为预置音色 ID  **/
data class TtsAudioConfig(
    val format: String = "wav",
    val voice: String = "冰糖"
)
