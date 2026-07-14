package com.fantto.auralite.data.repository

import com.fantto.auralite.domain.repository.VoiceRecognitionRepository
import com.fantto.auralite.service.VoiceRecognitionService
import kotlinx.coroutines.flow.Flow

class VoiceRecognitionRepositoryImpl : VoiceRecognitionRepository {
    override val transcription: Flow<String>
        get() = VoiceRecognitionService.transcription

    override val isRunning: Flow<Boolean>
        get() = VoiceRecognitionService.isRunning
}
