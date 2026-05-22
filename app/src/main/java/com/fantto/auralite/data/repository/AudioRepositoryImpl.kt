package com.fantto.auralite.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.fantto.auralite.data.remote.api.TtsApiService
import com.fantto.auralite.data.remote.dto.TtsRequest
import com.fantto.auralite.domain.repository.AudioRepository
import com.fantto.auralite.domain.repository.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/** AudioRepository的实现类，负责处理文本转语音和音频播放的具体逻辑 **/
class AudioRepositoryImpl(
    private val context: Context,
    private val ttsApiService: TtsApiService
) : AudioRepository {

    private var audioTrack: AudioTrack? = null

    //tts语音合成：实时返回语音二进制流，通过flow发出数据块，供UI层播放
    override fun synthesizeSpeech(
        text: String,
        model: String,
        voice: String,
        speed: Float
    ): Flow<ByteArray> = flow {
        val request = TtsRequest(
            model = model,
            input = text,
            voice = voice,
            speed = speed
        )

        val response = ttsApiService.synthesizeSpeech(request)

        if (response.isSuccessful) {
            response.body()?.byteStream()?.use { stream ->
                val buffer = ByteArray(8192)
                var read: Int = 0
                while (coroutineContext.isActive && stream.read(buffer).also { read = it } != -1) {
                    emit(buffer.copyOf(read))
                }
            }
        } else {
            throw Exception("TTS request failed: ${response.code()}")
        }
    }.flowOn(Dispatchers.IO)

    //音频播放：接收语音二进制流，使用AudioTrack进行播放，并通过flow发出播放状态
    override fun playAudio(audioData: ByteArray): Flow<PlaybackState> = flow {
        emit(PlaybackState.Idle)

        val bufferSize = AudioTrack.getMinBufferSize(
            24000,
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
                    .setSampleRate(24000)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        audioTrack?.play()
        emit(PlaybackState.Playing)

        audioTrack?.write(audioData, 0, audioData.size)

        emit(PlaybackState.Completed)
    }.flowOn(Dispatchers.IO)

    override fun stopPlayback() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    override fun release() {
        stopPlayback()
    }
}