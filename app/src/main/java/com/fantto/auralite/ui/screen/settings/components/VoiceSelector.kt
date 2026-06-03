package com.fantto.auralite.ui.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class MimoVoice(
    val id: String,
    val name: String,
    val language: String,
    val gender: String
)

private val mimoVoices = listOf(
    MimoVoice("冰糖", "冰糖", "中文", "女"),
    MimoVoice("茉莉", "茉莉", "中文", "女"),
    MimoVoice("苏打", "苏打", "中文", "男"),
    MimoVoice("白桦", "白桦", "中文", "男"),
    MimoVoice("Mia", "Mia", "英文", "女"),
    MimoVoice("Chloe", "Chloe", "英文", "女"),
    MimoVoice("Milo", "Milo", "英文", "男"),
    MimoVoice("Dean", "Dean", "英文", "男")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceSelector(
    selectedVoice: String,
    onVoiceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "音色选择",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        listOf("中文", "英文").forEach { language ->
            Text(
                text = language,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mimoVoices
                    .filter { it.language == language }
                    .forEach { voice ->
                        FilterChip(
                            selected = voice.id == selectedVoice,
                            onClick = { onVoiceSelected(voice.id) },
                            label = { Text("${voice.name} (${voice.gender})") }
                        )
                    }
            }
        }
    }
}
