package com.fantto.auralite.data.repository

import android.util.Base64
import com.elvishew.xlog.XLog
import com.fantto.auralite.data.remote.api.TtsApiService
import com.fantto.auralite.data.remote.dto.MimoTtsRequest
import com.fantto.auralite.data.remote.dto.TtsAudioConfig
import com.fantto.auralite.data.remote.dto.TtsMessage
import com.fantto.auralite.domain.repository.AudioRepository
import com.fantto.auralite.domain.repository.PlaybackState
import com.fantto.auralite.util.AudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** AudioRepository的实现类，负责处理文本转语音和音频播放的具体逻辑 **/
class AudioRepositoryImpl(
    private val ttsApiService: TtsApiService,
    private val audioPlayer: AudioPlayer
) : AudioRepository {

    //tts语音合成：调用 MiMo API 获取 Base64 编码的音频数据并解码返回
    override fun synthesizeSpeech(
        text: String,
        model: String,
        voice: String,
        speed: Float
    ): Flow<ByteArray> = flow {
        val request = MimoTtsRequest(
            model = model,
            messages = listOf(
                TtsMessage(role = "assistant", content = text)
            ),
            audio = TtsAudioConfig(
                format = "wav",
                voice = voice
            )
        )

        XLog.d("AudioRepositoryImpl：合成语音 model=$model, voice=$voice")

        val response = ttsApiService.synthesizeSpeech(request)

        if (response.isSuccessful) {
            val audioBase64 = response.body()?.choices?.firstOrNull()
                ?.message?.audio?.data
                ?: throw Exception("响应中无音频数据")

            val audioBytes = Base64.decode(audioBase64, Base64.DEFAULT)
            XLog.d("AudioRepositoryImpl：音频解码完成，大小 ${audioBytes.size} bytes")
            emit(audioBytes)
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