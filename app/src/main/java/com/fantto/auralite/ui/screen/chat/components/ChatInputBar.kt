package com.fantto.auralite.ui.screen.chat.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fantto.auralite.ui.icons.send
import com.fantto.auralite.ui.icons.voice_info

@Composable
fun ChatInputBar(
    inputText: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isListening: Boolean,
    isSending: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("输入消息...") },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            enabled = !isSending
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (inputText.isBlank()) {
            IconButton(
                onClick = onVoiceClick,
                enabled = !isSending,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isListening) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = voice_info,
                    contentDescription = "语音输入",
                    tint = if (isListening) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            IconButton(
                onClick = onSendClick,
                enabled = !isSending,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = send,
                    contentDescription = "发送",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}