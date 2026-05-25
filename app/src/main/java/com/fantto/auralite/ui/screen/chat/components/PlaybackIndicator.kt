package com.fantto.auralite.ui.screen.chat.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

@Composable
fun PlaybackIndicator(
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onTogglePlayback,
            modifier = Modifier.height(32.dp).width(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                modifier = Modifier.height(18.dp).width(18.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        if (isPlaying) {
            AudioWaveAnimation(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(24.dp)
                    .width(48.dp)
            )
        }
    }
}

@Composable
private fun AudioWaveAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )

    val barCount = 5
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val barWidth = size.width / (barCount * 2)
        val cornerRadius = barWidth / 2

        for (i in 0 until barCount) {
            val phase = (animProgress + i * 0.2f) % 1f
            val heightFactor = 0.3f + 0.7f * kotlin.math.sin(phase * Math.PI).toFloat()

            val barHeight = size.height * heightFactor
            val x = i * barWidth * 2 + barWidth / 2
            val y = (size.height - barHeight) / 2

            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(cornerRadius)
            )
        }
    }
}