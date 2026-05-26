package com.fantto.auralite.data.repository

import com.elvishew.xlog.XLog
import com.fantto.auralite.data.remote.api.TtsApiService
import com.fantto.auralite.data.remote.dto.TtsRequest
import com.fantto.auralite.domain.repository.AudioRepository
import com.fantto.auralite.domain.repository.PlaybackState
import com.fantto.auralite.util.AudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/** AudioRepository的实现类，负责处理文本转语音和音频播放的具体逻辑 **/
class AudioRepositoryImpl(
    private val ttsApiService: TtsApiService,
    private val audioPlayer: AudioPlayer
) : AudioRepository {

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

    //音频播放：委托给AudioPlayer处理
    override fun playAudio(audioData: ByteArray): Flow<PlaybackState> {
        XLog.d("AudioRepositoryImpl：播放音频，大小 ${audioData.size} bytes")
        return audioPlayer.play(audioData)
    }

    override fun stopPlayback() {
        audioPlayer.stop()
    }

    override fun release() {
        audioPlayer.release()
    }
}