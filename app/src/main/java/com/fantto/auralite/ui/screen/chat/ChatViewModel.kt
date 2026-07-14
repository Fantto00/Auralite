package com.fantto.auralite.ui.screen.chat

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import com.fantto.auralite.domain.model.ChatState
import com.fantto.auralite.domain.repository.PlaybackState
import com.fantto.auralite.domain.repository.VoiceRecognitionRepository
import com.fantto.auralite.domain.usecase.chat.LoadConversationUseCase
import com.fantto.auralite.domain.usecase.chat.SaveConversationUseCase
import com.fantto.auralite.domain.usecase.llm.SendMessageUseCase
import com.fantto.auralite.domain.usecase.tts.SpeakTextUseCase
import com.fantto.auralite.util.NetworkMonitor
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val saveConversationUseCase: SaveConversationUseCase,
    private val loadConversationUseCase: LoadConversationUseCase,
    private val voiceRecognitionRepository: VoiceRecognitionRepository,
    private val networkMonitor: NetworkMonitor
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

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var currentStreamingMessageId: String? = null
    private var ttsJob: Job? = null
    private var sendJob: Job? = null
    private var retryJob: Job? = null
    private var loadConversationJob: Job? = null
    private var currentConversationId: String? = null

    init {
        observeVoiceRecognition()
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _isOnline.value = online
                XLog.d("XLog ChatViewModel：网络状态=$online")
                if (online) {
                    retryJob?.cancel()
                    retryJob = viewModelScope.launch {
                        retryPendingMessages()
                    }
                }
            }
        }
    }

    private suspend fun retryPendingMessages() {
        if (_isSending.value) return
        if (sendMessageUseCase.hasPendingMessages()) {
            XLog.d("XLog ChatViewModel：网络恢复，重试待发送消息")
            _isSending.value = true
            sendMessageUseCase.flushPendingMessages().collect { state ->
                _chatState.value = state
                when (state) {
                    is ChatState.Loading -> {
                        finishStreaming()
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

                    is ChatState.Streaming -> updateStreamingMessage(state.content)

                    is ChatState.Complete -> {
                        finishStreaming()
                        _isSending.value = false
                        saveConversation()
                    }

                    is ChatState.Error -> {
                        finishStreaming()
                        _isSending.value = false
                        _errorMessage.value = state.message
                        XLog.e("XLog ChatViewModel：离线消息发送失败 ${state.message}")
                    }

                    else -> Unit
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeVoiceRecognition() {
        viewModelScope.launch {
            voiceRecognitionRepository.transcription
                .filter { it.isNotEmpty() }
                .debounce(300)
                .collect { result ->
                    _inputText.value = result
                    XLog.d("XLog ChatViewModel：识别结果填入 $result")
                }
        }
        viewModelScope.launch {
            voiceRecognitionRepository.isRunning.collect { running ->
                _isListening.value = running
            }
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage(text: String) {
        val cleanedText = text.replace(" ", "")
        if (cleanedText.isBlank() || _isSending.value) return

        sendJob?.cancel()
        stopSpeaking()

        val userMessage = MessageUiModel(
            id = UUID.randomUUID().toString(),
            content = cleanedText,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage
        _inputText.value = ""
        _errorMessage.value = null

        sendJob = viewModelScope.launch {
            _isSending.value = true
            try {
                sendMessageUseCase(cleanedText).collect { state ->
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

                        is ChatState.Streaming -> updateStreamingMessage(state.content)

                        is ChatState.Complete -> {
                            finishStreaming()
                            _isSending.value = false
                            saveConversation()
                        }

                        is ChatState.Error -> {
                            _errorMessage.value = state.message
                            finishStreaming()
                            _isSending.value = false
                            XLog.e("XLog ChatViewModel：发送失败 ${state.message}")
                        }

                        is ChatState.Pending -> {
                            _isSending.value = false
                            _messages.value = _messages.value.filterNot {
                                !it.isFromUser && it.content.isEmpty()
                            }
                            _errorMessage.value = "网络已断开，消息将在恢复连接后自动发送"
                        }
                    }
                }
            } catch (e: IOException) {
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
            _isPlaying.value = true
            try {
                speakTextUseCase(lastAiMessage.content).collect { state ->
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

                        else -> Unit
                    }
                }
            } catch (e: IOException) {
                _isPlaying.value = false
                _errorMessage.value = "语音播放失败"
                XLog.e("XLog ChatViewModel：TTS异常 ${e.message}")
            }
        }
    }

    fun stopSpeaking() {
        ttsJob?.cancel()
        speakTextUseCase.stop()
        _isPlaying.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private suspend fun saveConversation() {
        try {
            currentConversationId = saveConversationUseCase(currentConversationId)
            XLog.d("XLog ChatViewModel：对话已保存，id=$currentConversationId")
        } catch (e: SQLiteException) {
            XLog.e("XLog ChatViewModel：保存对话失败 ${e.message}")
        }
    }

    fun loadConversation(conversationId: String) {
        if (currentConversationId == conversationId && loadConversationJob?.isActive == true) return

        loadConversationJob?.cancel()
        currentConversationId = conversationId
        loadConversationJob = viewModelScope.launch {
            try {
                _messages.value = emptyList()
                loadConversationUseCase(conversationId).collect { messages ->
                    _messages.value = messages.map { message ->
                        MessageUiModel(
                            id = message.id,
                            content = message.content,
                            isFromUser = message.role == "user",
                            timestamp = message.timestamp
                        )
                    }
                    XLog.d("XLog ChatViewModel：加载了 ${messages.size} 条消息")
                }
            } catch (e: SQLiteException) {
                XLog.e("XLog ChatViewModel：加载对话失败 ${e.message}")
            }
        }
    }

    fun clearConversation() {
        sendJob?.cancel()
        retryJob?.cancel()
        loadConversationJob?.cancel()
        stopSpeaking()
        _isSending.value = false
        sendMessageUseCase.clearHistory()
        _messages.value = emptyList()
        _chatState.value = ChatState.Complete
        _errorMessage.value = null
        currentConversationId = null
        XLog.d("XLog ChatViewModel：清空对话")
        viewModelScope.launch {
            sendMessageUseCase.clearOfflineQueue()
        }
    }

    override fun onCleared() {
        sendJob?.cancel()
        retryJob?.cancel()
        loadConversationJob?.cancel()
        stopSpeaking()
        super.onCleared()
    }
}
