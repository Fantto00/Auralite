package com.fantto.auralite.data.remote.dto

/** MiMo TTS 响应的DTO（数据传输对象）类，包含选择列表  **/
data class MimoTtsResponse(
    val choices: List<TtsChoice>?
)

/** TTS 选择类，包含生成的音频消息  **/
data class TtsChoice(
    val message: TtsResponseMessage?
)

/** TTS 响应消息类，包含音频数据  **/
data class TtsResponseMessage(
    val audio: TtsAudioData?
)

/** TTS 音频数据类，data 为 Base64 编码的音频字符串  **/
data class TtsAudioData(
    val data: String?
)
