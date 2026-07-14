package com.fantto.auralite.domain.usecase.tts

import com.fantto.auralite.domain.repository.PlaybackState
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class SpeakTextUseCase(
    private val synthesizeSpeechUseCase: SynthesizeSpeechUseCase,
    private val playAudioUseCase: PlayAudioUseCase
) {
    operator fun invoke(text: String): Flow<PlaybackState> = flow {
        val audioData = ByteArrayOutputStream().use { output ->
            synthesizeSpeechUseCase(text).collect(output::write)
            output.toByteArray()
        }

        emitAll(playAudioUseCase(audioData))
    }

    fun stop() {
        playAudioUseCase.stop()
    }
}
