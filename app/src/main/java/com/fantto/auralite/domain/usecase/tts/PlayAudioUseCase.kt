package com.fantto.auralite.domain.usecase.tts

import com.elvishew.xlog.XLog
import com.fantto.auralite.domain.repository.AudioRepository
import com.fantto.auralite.domain.repository.PlaybackState
import kotlinx.coroutines.flow.Flow
/** 播放音频的用例，负责调用AudioRepository的playAudio方法来播放音频数据，并提供停止播放的功能 **/
class PlayAudioUseCase(
    private val audioRepository: AudioRepository
) {
    operator fun invoke(audioData: ByteArray): Flow<PlaybackState> {
        XLog.d("PlayAudioUseCase：播放音频，大小 ${audioData.size} bytes")
        return audioRepository.playAudio(audioData)
    }

    fun stop() {
        XLog.d("PlayAudioUseCase：停止播放")
        audioRepository.stopPlayback()
    }
}