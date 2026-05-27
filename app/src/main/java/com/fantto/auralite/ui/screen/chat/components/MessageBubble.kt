package com.fantto.auralite.ui.screen.chat.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.fantto.auralite.ui.icons.ai_icon
import com.fantto.auralite.ui.icons.person_raised_hand
import com.fantto.auralite.ui.screen.chat.MessageUiModel

// AI消息气泡组件，支持文本加粗和流式文本显示
@Composable
fun MessageBubble(
    message: MessageUiModel,
    isPlaying: Boolean = false,
    onTogglePlayback: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.isFromUser

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ai_icon,
                    contentDescription = "AI",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .animateContentSize()
                    .padding(12.dp)
            ) {
                if (message.isStreaming) {
                    StreamingText(
                        text = message.content,
                        isUser = isUser
                    )
                } else {
                    Text(
                        text = parseMarkdownBold(message.content),
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (!isUser && !message.isStreaming && message.content.isNotEmpty()) {
                PlaybackIndicator(
                    isPlaying = isPlaying,
                    onTogglePlayback = onTogglePlayback,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = person_raised_hand,
                    contentDescription = "User",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun StreamingText(
    text: String,
    isUser: Boolean
) {
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant

    if (text.isEmpty()) {
        val infiniteTransition = rememberInfiniteTransition(label = "cursor")
        val cursorAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cursorAlpha"
        )

        Text(
            text = "▋",
            color = textColor.copy(alpha = cursorAlpha),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        Row {
            Text(
                text = parseMarkdownBold(text),
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
            val infiniteTransition = rememberInfiniteTransition(label = "cursor")
            val cursorAlpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "cursorAlpha"
            )
            Text(
                text = "▋",
                color = textColor.copy(alpha = cursorAlpha),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// 简单的Markdown加粗解析器，支持**加粗**语法
private val BOLD_REGEX = Regex("\\*\\*(.+?)\\*\\*", RegexOption.DOT_MATCHES_ALL)

private fun parseMarkdownBold(text: String) = buildAnnotatedString {
    var lastIndex = 0
    
    BOLD_REGEX.findAll(text).forEach { match ->
        append(text.substring(lastIndex, match.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        lastIndex = match.range.last + 1
    }
    
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}