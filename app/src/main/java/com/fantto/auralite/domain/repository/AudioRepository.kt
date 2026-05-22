package com.fantto.auralite.domain.repository

import kotlinx.coroutines.flow.Flow

/** 音频相关的仓库接口，定义了文本转语音和音频播放的功能 **/
interface AudioRepository {

    fun synthesizeSpeech(
        text: String,
        model: String,
        voice: String,
        speed: Float
    ): Flow<ByteArray>

    fun playAudio(audioData: ByteArray): Flow<PlaybackState>

    fun stopPlayback()

    fun release()
}

sealed class PlaybackState {
    data object Idle : PlaybackState()
    data object Playing : PlaybackState()
    data object Completed : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}