package com.fantto.auralite.domain.repository

import kotlinx.coroutines.flow.Flow

interface VoiceRecognitionRepository {
    val transcription: Flow<String>
    val isRunning: Flow<Boolean>
}
