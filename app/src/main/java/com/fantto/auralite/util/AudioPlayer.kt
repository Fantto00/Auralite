package com.fantto.auralite.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.elvishew.xlog.XLog
import com.fantto.auralite.domain.repository.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** AudioPlayer类，负责处理音频播放的具体逻辑，包括播放控制和状态管理 **/
class AudioPlayer {

    companion object {
        private const val SAMPLE_RATE = 24000
    }

    private var audioTrack: AudioTrack? = null

    fun play(audioData: ByteArray): Flow<PlaybackState> = flow {
        emit(PlaybackState.Idle)

        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        audioTrack?.play()
        XLog.d("AudioPlayer：开始播放")
        emit(PlaybackState.Playing)

        audioTrack?.write(audioData, 0, audioData.size)

        emit(PlaybackState.Completed)
        XLog.d("AudioPlayer：播放完成")
    }.flowOn(Dispatchers.IO)

    fun stop() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        XLog.d("AudioPlayer：停止播放")
    }

    fun isPlaying(): Boolean {
        return audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING
    }

    fun release() {
        stop()
    }
}