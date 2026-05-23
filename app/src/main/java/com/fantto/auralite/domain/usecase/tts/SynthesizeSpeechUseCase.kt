package com.fantto.auralite.domain.usecase.tts

import com.elvishew.xlog.XLog
import com.fantto.auralite.domain.repository.AudioRepository
import com.fantto.auralite.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
/** 合成语音的用例类，负责将文本转换为语音数据，调用AudioRepository进行语音合成，并根据SettingsRepository获取相关配置 **/
class SynthesizeSpeechUseCase(
    private val audioRepository: AudioRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(text: String): Flow<ByteArray> {
        val model = settingsRepository.ttsModel.first()
        val voice = settingsRepository.ttsVoice.first()
        val speed = settingsRepository.ttsSpeed.first()

        XLog.d("SynthesizeSpeechUseCase：合成语音 model=$model, voice=$voice, speed=$speed")

        return audioRepository.synthesizeSpeech(text, model, voice, speed)
    }
}