package com.fantto.auralite.domain.engine

import kotlinx.coroutines.flow.Flow

/** Vosk语音识别引擎接口，定义了初始化、开始/停止监听、观察转录结果和部分结果、检查是否正在监听以及释放资源的功能 **/
interface SttEngine {

    suspend fun initialize()

    fun startListening()

    fun stopListening()

    fun observeTranscription(): Flow<String>

    fun observePartialResult(): Flow<String>

    fun isListening(): Boolean

    fun release()
}