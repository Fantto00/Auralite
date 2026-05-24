package com.fantto.auralite.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import com.fantto.auralite.domain.model.ChatState
import com.fantto.auralite.domain.repository.SettingsRepository
import com.fantto.auralite.domain.usecase.llm.SendMessageUseCase
import com.fantto.auralite.domain.usecase.tts.PlayAudioUseCase
import com.fantto.auralite.domain.usecase.tts.SynthesizeSpeechUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val synthesizeSpeechUseCase: SynthesizeSpeechUseCase,
    private val playAudioUseCase: PlayAudioUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageUiModel>>(emptyList())
    val messages: StateFlow<List<MessageUiModel>> = _messages.asStateFlow()

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Complete)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var currentStreamingMessageId: String? = null
    private var ttsJob: Job? = null

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = MessageUiModel(
            id = UUID.randomUUID().toString(),
            content = text,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage
        _inputText.value = ""

        viewModelScope.launch {
            try {
                sendMessageUseCase(text).collect { state ->
                    _chatState.value = state
                    when (state) {
                        is ChatState.Loading -> {
                            val aiMessage = MessageUiModel(
                                id = UUID.randomUUID().toString(),
                                content = "",
                                isFromUser = false,
                                timestamp = System.currentTimeMillis(),
                                isStreaming = true
                            )
                            currentStreamingMessageId = aiMessage.id
                            _messages.value = _messages.value + aiMessage
                        }
                        is ChatState.Streaming -> {
                            updateStreamingMessage(state.content)
                        }
                        is ChatState.Complete -> {
                            finishStreaming()
                            XLog.d("ChatViewModel：消息发送完成")
                        }
                        is ChatState.Error -> {
                            updateStreamingMessage("错误: ${state.message}")
                            finishStreaming()
                            XLog.e("ChatViewModel：发送失败 ${state.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                XLog.e("ChatViewModel：异常 ${e.message}")
                _chatState.value = ChatState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun updateStreamingMessage(content: String) {
        val messageId = currentStreamingMessageId ?: return
        _messages.value = _messages.value.map { message ->
            if (message.id == messageId) {
                message.copy(content = content, isStreaming = true)
            } else {
                message
            }
        }
    }

    private fun finishStreaming() {
        val messageId = currentStreamingMessageId ?: return
        _messages.value = _messages.value.map { message ->
            if (message.id == messageId) {
                message.copy(isStreaming = false)
            } else {
                message
            }
        }
        currentStreamingMessageId = null
    }

    fun speakLastMessage() {
        val lastAiMessage = _messages.value.lastOrNull { !it.isFromUser } ?: return

        ttsJob?.cancel()
        ttsJob = viewModelScope.launch {
            try {
                _isPlaying.value = true
                val audioFlow = synthesizeSpeechUseCase(lastAiMessage.content)
                val audioData = mutableListOf<Byte>()

                audioFlow.collect { chunk ->
                    audioData.addAll(chunk.toList())
                }

                playAudioUseCase(audioData.toByteArray()).collect { state ->
                    when (state) {
                        is com.fantto.auralite.domain.repository.PlaybackState.Completed -> {
                            _isPlaying.value = false
                        }
                        is com.fantto.auralite.domain.repository.PlaybackState.Error -> {
                            _isPlaying.value = false
                            XLog.e("ChatViewModel：播放失败 ${state.message}")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _isPlaying.value = false
                XLog.e("ChatViewModel：TTS异常 ${e.message}")
            }
        }
    }

    fun stopSpeaking() {
        ttsJob?.cancel()
        playAudioUseCase.stop()
        _isPlaying.value = false
    }

    fun clearConversation() {
        sendMessageUseCase.clearHistory()
        _messages.value = emptyList()
        _chatState.value = ChatState.Complete
        XLog.d("ChatViewModel：清空对话")
    }

    override fun onCleared() {
        super.onCleared()
        stopSpeaking()
    }
}