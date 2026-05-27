package com.fantto.auralite.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import com.fantto.auralite.data.remote.dto.ChatMessage
import com.fantto.auralite.domain.model.ChatState
import com.fantto.auralite.domain.repository.ChatRepository
import com.fantto.auralite.domain.repository.PlaybackState
import com.fantto.auralite.domain.repository.SettingsRepository
import com.fantto.auralite.domain.usecase.llm.SendMessageUseCase
import com.fantto.auralite.domain.usecase.tts.PlayAudioUseCase
import com.fantto.auralite.domain.usecase.tts.SynthesizeSpeechUseCase
import com.fantto.auralite.service.VoiceRecognitionService
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
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository
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

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentStreamingMessageId: String? = null
    private var ttsJob: Job? = null
    private var sendJob: Job? = null

    init {
        observeVoiceRecognition()
    }

    private fun observeVoiceRecognition() {
        viewModelScope.launch {
            VoiceRecognitionService.transcription.collect { result ->
                if (result.isNotEmpty()) {
                    _inputText.value = result
                    XLog.d("XLog ChatViewModel：识别结果填入 $result")
                }
            }
        }
        viewModelScope.launch {
            VoiceRecognitionService.isRunning.collect { running ->
                _isListening.value = running
            }
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isSending.value) return

        sendJob?.cancel()
        ttsJob?.cancel()
        _isPlaying.value = false
        playAudioUseCase.stop()

        val userMessage = MessageUiModel(
            id = UUID.randomUUID().toString(),
            content = text,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage
        _inputText.value = ""
        _errorMessage.value = null

        sendJob = viewModelScope.launch {
            _isSending.value = true
            try {
                XLog.d("XLog ChatViewModel：collect消息流")
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
                            _isSending.value = false
                            XLog.d("XLog ChatViewModel：消息发送完成")
                            // 保存对话到数据库
                            saveConversation()
                        }
                        is ChatState.Error -> {
                            _errorMessage.value = state.message
                            finishStreaming()
                            _isSending.value = false
                            XLog.e("XLog ChatViewModel：发送失败 ${state.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isSending.value = false
                XLog.e("XLog ChatViewModel：异常 ${e.message}")
            }
        }
    }

    fun retryLastMessage() {
        val lastUserMessage = _messages.value.lastOrNull { it.isFromUser } ?: return
        _messages.value = _messages.value.dropLast(1)
        sendMessage(lastUserMessage.content)
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

        if (_isPlaying.value) {
            stopSpeaking()
            return
        }

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
                        is PlaybackState.Completed -> {
                            _isPlaying.value = false
                            XLog.d("XLog ChatViewModel：播放完成")
                        }
                        is PlaybackState.Error -> {
                            _isPlaying.value = false
                            _errorMessage.value = "播放失败: ${state.message}"
                            XLog.e("XLog ChatViewModel：播放失败 ${state.message}")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _isPlaying.value = false
                XLog.e("XLog ChatViewModel：TTS异常 ${e.message}")
            }
        }
    }

    fun stopSpeaking() {
        ttsJob?.cancel()
        playAudioUseCase.stop()
        _isPlaying.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun saveConversation() {
        viewModelScope.launch {
            try {
                val history = sendMessageUseCase.getHistory()
                if (history.isNotEmpty()) {
                    // 使用用户的第一条消息作为标题
                    val title = history.firstOrNull { it.role == "user" }?.content?.take(50) ?: "新对话"
                    chatRepository.saveConversation(title, history)
                    XLog.d("XLog ChatViewModel：对话已保存，标题=$title")
                }
            } catch (e: Exception) {
                XLog.e("XLog ChatViewModel：保存对话失败 ${e.message}")
            }
        }
    }

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                // 清空当前对话
                sendMessageUseCase.clearHistory()
                _messages.value = emptyList()
                
                // 从数据库加载消息
                chatRepository.getMessagesByConversationId(conversationId).collect { messageEntities ->
                    val chatMessages = messageEntities.map { entity ->
                        ChatMessage(role = entity.role, content = entity.content)
                    }
                    
                    // 设置SendMessageUseCase的历史记录
                    sendMessageUseCase.setHistory(chatMessages)
                    
                    // 转换为UI模型并更新
                    val uiMessages = messageEntities.map { entity ->
                        MessageUiModel(
                            id = entity.id,
                            content = entity.content,
                            isFromUser = entity.role == "user",
                            timestamp = entity.timestamp
                        )
                    }
                    _messages.value = uiMessages
                    XLog.d("XLog ChatViewModel：加载了 ${uiMessages.size} 条消息")
                }
            } catch (e: Exception) {
                XLog.e("XLog ChatViewModel：加载对话失败 ${e.message}")
            }
        }
    }

    fun clearConversation() {
        sendJob?.cancel()
        ttsJob?.cancel()
        _isSending.value = false
        _isPlaying.value = false
        playAudioUseCase.stop()
        sendMessageUseCase.clearHistory()
        _messages.value = emptyList()
        _chatState.value = ChatState.Complete
        _errorMessage.value = null
        XLog.d("XLog ChatViewModel：清空对话")
    }

    override fun onCleared() {
        super.onCleared()
        sendJob?.cancel()
        ttsJob?.cancel()
        playAudioUseCase.stop()
    }
}