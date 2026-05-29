package com.fantto.auralite.ui.screen.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fantto.auralite.domain.model.ChatState
import com.fantto.auralite.service.VoiceRecognitionService
import com.fantto.auralite.ui.icons.delete
import com.fantto.auralite.ui.screen.chat.components.ChatInputBar
import com.fantto.auralite.ui.screen.chat.components.MessageBubble
import com.fantto.auralite.util.PermissionHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Auralite") },
                windowInsets = WindowInsets(0),
                actions = {
                    IconButton(onClick = { viewModel.clearConversation() }) {
                        Icon(
                            imageVector = delete,
                            contentDescription = "清空对话"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(4.dp),
                        strokeWidth = 2.dp
                    )
                }

                ChatInputBar(
                    inputText = inputText,
                    onTextChange = viewModel::updateInputText,
                    onSendClick = { viewModel.sendMessage(inputText) },
                    onVoiceClick = {
                        if (isListening) {
                            VoiceRecognitionService.stopService(context)
                        } else {
                            // 工具类检查权限
                            PermissionHelper.requestRecordAudioPermission(
                                context = context,
                                onGranted = {
                                    VoiceRecognitionService.startService(context)
                                },
                                onDenied = { doNotAskAgain ->
                                    coroutineScope.launch {
                                        if (doNotAskAgain) {
                                            snackbarHostState.showSnackbar(
                                                "麦克风权限被永久拒绝，请前往设置手动开启"
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                "需要麦克风权限才能使用语音识别"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    },
                    isListening = isListening,
                    isSending = isSending
                )
            }
        }
    ) { padding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "输入消息开始对话",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isPlaying = isPlaying && !message.isFromUser && message == messages.lastOrNull { !it.isFromUser },
                        onTogglePlayback = { viewModel.speakLastMessage() }
                    )
                }

                if (chatState is ChatState.Error) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { viewModel.retryLastMessage() }) {
                                Text("重试")
                            }
                        }
                    }
                }
            }
        }
    }
}