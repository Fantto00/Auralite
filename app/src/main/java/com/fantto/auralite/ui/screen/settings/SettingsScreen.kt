package com.fantto.auralite.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fantto.auralite.ui.icons.arrow_back_ios
import com.fantto.auralite.ui.screen.settings.components.ApiConfigItem
import com.fantto.auralite.ui.screen.settings.components.SliderItem
import com.fantto.auralite.ui.screen.settings.components.VoiceSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit = {}
) {
    val llmBaseUrl by viewModel.llmBaseUrl.collectAsState()
    val llmApiKey by viewModel.llmApiKey.collectAsState()
    val llmModel by viewModel.llmModel.collectAsState()
    val ttsApiKey by viewModel.ttsApiKey.collectAsState()
    val ttsModel by viewModel.ttsModel.collectAsState()
    val ttsVoice by viewModel.ttsVoice.collectAsState()
    val ttsSpeed by viewModel.ttsSpeed.collectAsState()
    val sttLanguage by viewModel.sttLanguage.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar("配置已保存")
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = arrow_back_ios,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader(title = "LLM 配置")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                ApiConfigItem(
                    title = "Base URL",
                    value = llmBaseUrl,
                    onValueChange = viewModel::updateLlmBaseUrl,
                    placeholder = "example:https://api.openai.com"
                )
                ApiConfigItem(
                    title = "API Key",
                    value = llmApiKey,
                    onValueChange = viewModel::updateLlmApiKey,
                    placeholder = "sk-...",
                    isPassword = true
                )
                ApiConfigItem(
                    title = "Model",
                    value = llmModel,
                    onValueChange = viewModel::updateLlmModel,
                    placeholder = "gpt-3.5-turbo"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "TTS 配置")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                ApiConfigItem(
                    title = "API Key",
                    value = ttsApiKey,
                    onValueChange = viewModel::updateTtsApiKey,
                    placeholder = "TTS API Key",
                    isPassword = true
                )
                ApiConfigItem(
                    title = "Model",
                    value = ttsModel,
                    onValueChange = viewModel::updateTtsModel,
                    placeholder = "tts-1"
                )
                VoiceSelector(
                    selectedVoice = ttsVoice,
                    onVoiceSelected = viewModel::updateTtsVoice
                )
                SliderItem(
                    title = "语速",
                    value = ttsSpeed,
                    onValueChange = viewModel::updateTtsSpeed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "STT 配置")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                LanguageSelector(
                    selectedLanguage = sttLanguage,
                    onLanguageSelected = viewModel::updateSttLanguage
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveAllConfig() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("保存配置")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("zh" to "中文", "en" to "英文", "ja" to "日文")

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "识别语言",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            OutlinedTextField(
                value = languages.find { it.first == selectedLanguage }?.second ?: selectedLanguage,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onLanguageSelected(code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}