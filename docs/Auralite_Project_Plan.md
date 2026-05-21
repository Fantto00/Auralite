# Auralite 项目规划文档

## 📋 项目概述

**项目名称**: Auralite - 智能语音助手  
**项目定位**: 在线+离线混合的 AI 语音助手 Android 应用  
**核心特点**: 离线语音识别 + 在线 AI 对话 + 在线语音合成

---

## 🎯 功能需求

### 核心功能模块

#### 1. 语音输入模块（离线 STT）
- **引擎**: Vosk（离线语音识别）
- **功能**:
  - 实时语音流式识别
  - 中文/英文语言支持
  - 语音活动检测（VAD）
  - 识别结果实时显示

#### 2. AI 对话模块（在线 LLM）
- **接口**: OpenAI 兼容 API（用户自配置）
- **功能**:
  - 多轮对话支持
  - 流式响应显示
  - API Key 管理
  - 多模型切换
  - 对话历史管理

#### 3. 语音输出模块（在线 TTS）
- **引擎**: 小米 MiMo TTS
- **功能**:
  - 文字转语音合成
  - 流式音频播放
  - 多音色选择
  - 语速/音量调节

#### 4. 用户设置模块
- **功能**:
  - LLM API 配置（Base URL、API Key、Model）
  - MiMo TTS API 配置
  - 语音识别语言设置
  - TTS 音色/语速设置
  - 主题/深色模式

---

## 🏗️ 技术架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                   Jetpack Compose UI                 │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐           │   │
│  │  │ Chat     │ │ Settings │ │ Voice    │           │   │
│  │  │ Screen   │ │ Screen   │ │ Widget   │           │   │
│  │  └──────────┘ └──────────┘ └──────────┘           │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    ViewModels                        │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐           │   │
│  │  │ ChatVM   │ │ Settings │ │ AudioVM  │           │   │
│  │  └──────────┘ └──────────┘ └──────────┘           │   │
│  └─────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                           │
│  ┌─────────────────────────────────────────────────────┐   │
│  │               Use Cases & Models                     │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐           │   │
│  │  │ STT      │ │ LLM      │ │ TTS      │           │   │
│  │  │ UseCase  │ │ UseCase  │ │ UseCase  │           │   │
│  │  └──────────┘ └──────────┘ └──────────┘           │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Repository Interfaces                   │   │
│  └─────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                       Data Layer                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ Vosk     │ │ LLM API  │ │ MiMo API │ │ Local    │   │
│  │ Engine   │ │ Service  │ │ Service  │ │ Storage  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈详情

| 层级 | 技术 | 用途 |
|------|------|------|
| UI | Jetpack Compose + Material 3 | 声明式 UI |
| 架构 | MVVM + Clean Architecture | 代码组织 |
| 异步 | Kotlin Coroutines + Flow | 并发处理 |
| 网络 | OkHttp + Retrofit | API 调用 |
| 本地存储 | DataStore + Room | 配置和历史 |
| 音频 | AudioRecord + AudioTrack | 录音播放 |
| STT | Vosk Android | 离线语音识别 |
| TTS | MiMo API（OpenAI 兼容）（预留后续Qwen TTS API) | 在线语音合成 |

---

## 📁 分包策略

### 项目根包名: `com.fantto.auralite`

```
app/
├── src/main/java/com/fantto/auralite/
│   ├── App.kt                          # Application 类，全局初始化
│   ├── MainActivity.kt                 # 主 Activity
│   │
│   ├── data/                           # 数据层
│   │   ├── local/                     # 本地数据源
│   │   │   ├── dao/                   # Room DAO
│   │   │   │   └── ConversationDao.kt
│   │   │   ├── datastore/            # DataStore
│   │   │   │   └── SettingsDataStore.kt
│   │   │   └── entity/               # 数据库实体
│   │   │       └── ConversationEntity.kt
│   │   │
│   │   ├── remote/                    # 远程数据源
│   │   │   ├── api/                   # API 接口
│   │   │   │   ├── LlmApiService.kt
│   │   │   │   └── TtsApiService.kt
│   │   │   ├── dto/                   # 数据传输对象
│   │   │   │   ├── LlmRequest.kt
│   │   │   │   ├── LlmResponse.kt
│   │   │   │   ├── TtsRequest.kt
│   │   │   │   └── TtsResponse.kt
│   │   │   └── interceptor/          # 拦截器
│   │   │       └── AuthInterceptor.kt
│   │   │
│   │   ├── engine/                    # 引擎封装
│   │   │   ├── stt/                   # 语音识别引擎
│   │   │   │   ├── SttEngine.kt       # 接口
│   │   │   │   └── VoskEngine.kt      # Vosk 实现
│   │   │   └── tts/                   # 语音合成引擎
│   │   │       ├── TtsEngine.kt       # 接口
│   │   │       └── MiMoEngine.kt      # MiMo 实现
│   │   │
│   │   ├── model/                     # 数据模型
│   │   │   ├── Message.kt
│   │   │   ├── Conversation.kt
│   │   │   ├── AudioConfig.kt
│   │   │   └── ApiConfig.kt
│   │   │
│   │   └── repository/                # 仓库实现
│   │       ├── ChatRepositoryImpl.kt
│   │       ├── SettingsRepositoryImpl.kt
│   │       └── AudioRepositoryImpl.kt
│   │
│   ├── domain/                        # 领域层
│   │   ├── model/                     # 领域模型
│   │   │   ├── Message.kt
│   │   │   ├── Role.kt               # USER, ASSISTANT, SYSTEM
│   │   │   └── ChatState.kt
│   │   │
│   │   ├── repository/                # 仓库接口
│   │   │   ├── ChatRepository.kt
│   │   │   ├── SettingsRepository.kt
│   │   │   └── AudioRepository.kt
│   │   │
│   │   └── usecase/                   # 用例
│   │       ├── stt/
│   │       │   ├── StartListeningUseCase.kt
│   │       │   ├── StopListeningUseCase.kt
│   │       │   └── ObserveTranscriptionUseCase.kt
│   │       ├── llm/
│   │       │   ├── SendMessageUseCase.kt
│   │       │   ├── StreamChatUseCase.kt
│   │       │   └── GetConversationUseCase.kt
│   │       └── tts/
│   │           ├── SynthesizeSpeechUseCase.kt
│   │           └── PlayAudioUseCase.kt
│   │
│   ├── presentation/                  # 表现层
│   │   ├── navigation/               # 导航
│   │   │   ├── NavGraph.kt
│   │   │   └── Screen.kt
│   │   │
│   │   ├── screen/                    # 页面
│   │   │   ├── chat/                 # 聊天页面
│   │   │   │   ├── ChatScreen.kt
│   │   │   │   ├── ChatViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── MessageBubble.kt
│   │   │   │       ├── VoiceInputButton.kt
│   │   │   │       └── ChatInputBar.kt
│   │   │   │
│   │   │   ├── settings/             # 设置页面
│   │   │   │   ├── SettingsScreen.kt
│   │   │   │   ├── SettingsViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── ApiConfigItem.kt
│   │   │   │       └── VoiceSelector.kt
│   │   │   │
│   │   │   └── conversation/         # 对话历史
│   │   │       ├── ConversationScreen.kt
│   │   │       └── ConversationViewModel.kt
│   │   │
│   │   └── theme/                    # 主题
│   │       ├── Color.kt
│   │       ├── Type.kt
│   │       └── Theme.kt
│   │
│   ├── di/                           # 手动依赖注入
│   │   ├── AppModule.kt             # 提供全局单例
│   │   └── ViewModelFactory.kt      # ViewModel 工厂
│   │
│   └── util/                         # 工具类
│       ├── AudioManager.kt          # 音频管理
│       ├── NetworkUtil.kt           # 网络工具
│       └── Extensions.kt            # 扩展函数
│
├── src/main/assets/                  # 资源文件
│   └── vosk-model-cn/               # Vosk 中文模型
│
└── src/main/res/                     # Android 资源
    ├── values/
    │   ├── strings.xml
    └── raw/                          # 原始资源
```

---

## 📅 开发步骤

### Phase 1: 项目初始化（2-3 天）

#### 1.1 创建项目
- [ ] 使用 Android Studio 创建新项目
  - 项目名称: Auralite
  - 包名: com.fantto.auralite
  - 语言: Kotlin
  - 最低 SDK: API 26 (Android 8.0)
- [ ] 配置 Gradle（Kotlin DSL）
- [ ] 添加核心依赖：
  ```kotlin
  // build.gradle.kts
  dependencies {
      // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // Vosk
    implementation("com.alphacephei:vosk-android:0.3.45")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
  }
  ```

#### 1.2 搭建基础架构
- [ ] 创建 Application 类（手动初始化依赖）
- [ ] 创建 AppModule 单例容器
- [ ] 创建基础 Theme
- [ ] 设置 Navigation

#### 1.3 创建基础 UI
- [ ] MainActivity + NavGraph
- [ ] 底部导航栏（聊天、历史、设置）
- [ ] 空页面占位

---

### Phase 2: 数据层开发（3-4 天）

#### 2.1 本地存储
- [ ] 创建 SettingsDataStore（API 配置存储）
  ```kotlin
  class SettingsDataStore(
      private val sharedPreferences: SharedPreferences
  ) {
      // LLM 配置
      val llmBaseUrl: Flow<String>
      val llmApiKey: Flow<String>
      val llmModel: Flow<String>
      
      // TTS 配置
      val ttsApiKey: Flow<String>
      val ttsVoice: Flow<String>
      val ttsSpeed: Flow<Float>
      
      // STT 配置
      val sttLanguage: Flow<String>
      
      suspend fun saveLlmConfig(baseUrl: String, apiKey: String, model: String)
      suspend fun saveTtsConfig(apiKey: String, voice: String, speed: Float)
  }
  ```

- [ ] 创建 Room 数据库（对话历史）
  ```kotlin
  @Database(entities = [ConversationEntity::class, MessageEntity::class], version = 1)
  abstract class AppDatabase : RoomDatabase() {
      abstract fun conversationDao(): ConversationDao
  }
  
  @Entity(tableName = "conversations")
  data class ConversationEntity(
      @PrimaryKey val id: String,
      val title: String,
      val createdAt: Long,
      val updatedAt: Long
  )
  
  @Entity(tableName = "messages")
  data class MessageEntity(
      @PrimaryKey val id: String,
      val conversationId: String,
      val role: String,  // user, assistant, system
      val content: String,
      val timestamp: Long
  )
  ```

#### 2.2 网络层
- [ ] 创建 LLM API Service
  ```kotlin
  interface LlmApiService {
      @POST("v1/chat/completions")
      @Streaming
      suspend fun streamChat(
          @Body request: LlmRequest
      ): Response<ResponseBody>
  }
  
  data class LlmRequest(
      val model: String,
      val messages: List<ChatMessage>,
      val stream: Boolean = true,
      val temperature: Float = 0.7f
  )
  ```

- [ ] 创建 MiMo TTS API Service
  ```kotlin
  interface TtsApiService {
      @POST("v1/audio/speech")
      @Streaming
      suspend fun synthesizeSpeech(
          @Body request: TtsRequest
      ): Response<ResponseBody>
  }
  
  data class TtsRequest(
      val model: String = "xiaomi-mimo-tts",
      val input: String,
      val voice: String = "alloy",
      val response_format: String = "mp3",
      val speed: Float = 1.0f
  )
  ```

- [ ] 配置 OkHttp（认证拦截器、超时配置）

#### 2.3 仓库实现
- [ ] ChatRepository（消息管理）
- [ ] SettingsRepository（配置管理）
- [ ] AudioRepository（音频管理）

---

### Phase 3: 离线 STT 集成（2-3 天）

#### 3.1 Vosk 引擎封装
- [ ] 下载 Vosk 中文模型
  ```bash
  # 下载模型
  wget https://alphacephei.com/vosk/models/vosk-model-cn-0.22.zip
  # 解压到 assets/vosk-model-cn/
  ```

- [ ] 创建 SttEngine 接口
  ```kotlin
  interface SttEngine {
      suspend fun initialize()
      fun startListening()
      fun stopListening()
      fun observeTranscription(): Flow<String>
      fun observePartialResult(): Flow<String>
      fun release()
  }
  ```

- [ ] 实现 VoskEngine
  ```kotlin
  class VoskEngine(
      private val context: Context
  ) : SttEngine {
      private var recognizer: Recognizer? = null
      private val _transcription = MutableSharedFlow<String>()
      private val _partialResult = MutableSharedFlow<String>()
      
      override suspend fun initialize() {
          // 从 assets 复制模型到内部存储
          // 初始化 Vosk Recognizer
      }
      
      override fun startListening() {
          // 开始录音和识别
      }
      
      override fun stopListening() {
          // 停止录音
      }
      
      override fun observeTranscription(): Flow<String> = _transcription
      override fun observePartialResult(): Flow<String> = _partialResult
  }
  ```

#### 3.2 音频录制管理
- [ ] 创建 AudioRecorder 封装
  ```kotlin
  class AudioRecorder {
      private var recorder: AudioRecord? = null
      
      fun startRecording(): Flow<ByteArray> = flow {
          // 配置 AudioRecord
          val bufferSize = AudioRecord.getMinBufferSize(
              16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
          )
          recorder = AudioRecord(
              MediaRecorder.AudioSource.MIC,
              16000, AudioFormat.CHANNEL_IN_MONO,
              AudioFormat.ENCODING_PCM_16BIT, bufferSize
          )
          recorder?.startRecording()
          
          val buffer = ByteArray(bufferSize)
          while (isActive) {
              val read = recorder?.read(buffer, 0, bufferSize) ?: 0
              if (read > 0) {
                  emit(buffer.copyOf(read))
              }
          }
      }.flowOn(Dispatchers.IO)
      
      fun stopRecording() {
          recorder?.stop()
          recorder?.release()
          recorder = null
      }
  }
  ```

---

### Phase 4: 在线 LLM 集成（2-3 天）

#### 4.1 LLM 用例
- [ ] StreamChatUseCase（流式对话）
  ```kotlin
  class StreamChatUseCase(
      private val chatRepository: ChatRepository
  ) {
      operator fun invoke(message: String): Flow<ChatState> = flow {
          emit(ChatState.Loading)
          
          chatRepository.sendMessage(message)
              .collect { chunk ->
                  emit(ChatState.Streaming(chunk))
              }
          
          emit(ChatState.Complete)
      }.catch { e ->
          emit(ChatState.Error(e.message ?: "Unknown error"))
      }
  }
  ```

#### 4.2 对话管理
- [ ] 多轮对话上下文维护
- [ ] 流式响应解析（SSE）
- [ ] 错误处理和重试

---

### Phase 5: 在线 TTS 集成（2-3 天）

#### 5.1 MiMo TTS 引擎
- [ ] 创建 TtsEngine 接口
  ```kotlin
  interface TtsEngine {
      suspend fun synthesize(text: String): Flow<ByteArray>
      fun stop()
      suspend fun setVoice(voice: String)
      suspend fun setSpeed(speed: Float)
  }
  ```

- [ ] 实现 MiMoEngine
  ```kotlin
  class MiMoEngine(
      private val ttsApiService: TtsApiService,
      private val settingsRepository: SettingsRepository
  ) : TtsEngine {
      
      override suspend fun synthesize(text: String): Flow<ByteArray> = flow {
          val config = settingsRepository.getTtsConfig()
          val request = TtsRequest(
              input = text,
              voice = config.voice,
              speed = config.speed
          )
          
          val response = ttsApiService.synthesizeSpeech(request)
          if (response.isSuccessful) {
              response.body()?.byteStream()?.use { stream ->
                  val buffer = ByteArray(8192)
                  var read: Int
                  while (stream.read(buffer).also { read = it } != -1) {
                      emit(buffer.copyOf(read))
                  }
              }
          } else {
              throw Exception("TTS failed: ${response.code()}")
          }
      }.flowOn(Dispatchers.IO)
  }
  ```

#### 5.2 音频播放器
- [ ] 创建 AudioPlayer 封装
  ```kotlin
  class AudioPlayer {
      private var player: AudioTrack? = null
      
      fun play(audioStream: Flow<ByteArray>): Flow<PlaybackState> = flow {
          // 初始化 AudioTrack
          player = AudioTrack.Builder()
              .setAudioAttributes(
                  AudioAttributes.Builder()
                      .setUsage(AudioAttributes.USAGE_MEDIA)
                      .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                      .build()
              )
              .setAudioFormat(
                  AudioFormat.Builder()
                      .setSampleRate(24000)
                      .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                      .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                      .build()
              )
              .setBufferSizeInBytes(bufferSize)
              .build()
          
          player?.play()
          emit(PlaybackState.Playing)
          
          audioStream.collect { chunk ->
              player?.write(chunk, 0, chunk.size)
          }
          
          emit(PlaybackState.Completed)
      }.flowOn(Dispatchers.IO)
      
      fun stop() {
          player?.stop()
          player?.release()
          player = null
      }
  }
  ```

---

### Phase 6: UI 开发（3-4 天）

#### 6.1 聊天页面
- [ ] ChatScreen（主界面）
  ```kotlin
  @Composable
  fun ChatScreen(
      viewModel: ChatViewModel
  ) {
      val messages by viewModel.messages.collectAsStateWithLifecycle()
      val chatState by viewModel.chatState.collectAsStateWithLifecycle()
      val isListening by viewModel.isListening.collectAsStateWithLifecycle()
      
      Scaffold(
          bottomBar = {
              ChatInputBar(
                  onSendMessage = viewModel::sendMessage,
                  onVoiceInput = viewModel::toggleVoiceInput,
                  isListening = isListening
              )
          }
      ) { padding ->
          LazyColumn(
              modifier = Modifier
                  .fillMaxSize()
                  .padding(padding)
          ) {
              items(messages) { message ->
                  MessageBubble(message = message)
              }
          }
      }
  }
  ```

- [ ] MessageBubble（消息气泡）
- [ ] VoiceInputButton（语音输入按钮，带动画）
- [ ] ChatInputBar（输入栏）

#### 6.2 设置页面
- [ ] SettingsScreen
- [ ] API 配置表单
- [ ] 音色选择器
- [ ] 语言选择器

#### 6.3 对话历史
- [ ] ConversationListScreen
- [ ] ConversationDetailScreen

---

### Phase 7: 功能整合与优化（2-3 天）

#### 7.1 核心流程整合
- [ ] 语音输入 → 文字识别 → AI 对话 → 语音播放 完整流程
- [ ] 流式处理优化（边识别边发送）
- [ ] 并发控制（协程 Scope 管理）

#### 7.2 用户体验优化
- [ ] 录音动画效果
- [ ] 流式文字显示动画
- [ ] 播放状态指示器
- [ ] 错误提示和重试机制

#### 7.3 性能优化
- [ ] 内存优化（及时释放音频资源）
- [ ] 网络优化（请求取消、超时处理）
- [ ] 电量优化（后台时暂停录音）

---

### Phase 8: 测试与发布（2-3 天）

#### 8.1 测试
- [ ] 单元测试（ViewModel、UseCase）
- [ ] 集成测试（API 调用）
- [ ] UI 测试（Compose Testing）

#### 8.2 打包发布
- [ ] 签名配置
- [ ] ProGuard 规则
- [ ] 生成 Release APK

---

## 🎯 里程碑

| 阶段 | 时间 | 交付物 |
|------|------|--------|
| Phase 1 | Day 1-3 | 项目骨架、基础 UI |
| Phase 2 | Day 4-7 | 数据层、网络层 |
| Phase 3 | Day 8-10 | Vosk 语音识别 |
| Phase 4 | Day 11-13 | LLM 对话功能 |
| Phase 5 | Day 14-16 | MiMo TTS 语音合成 |
| Phase 6 | Day 17-20 | 完整 UI |
| Phase 7 | Day 21-23 | 功能整合、优化 |
| Phase 8 | Day 24-26 | 测试、发布 |

**预计总工期**: 4 周（26 个工作日）

---

## ⚠️ 风险与应对

| 风险 | 影响 | 应对方案 |
|------|------|---------|
| Vosk 模型识别准确率低 | 用户体验差 | 提供手动输入选项 |
| MiMo API 不稳定 | 语音合成失败 | 添加重试机制、缓存 |
| 网络延迟高 | 响应慢 | 流式处理、加载提示 |
| 内存溢出 | 应用崩溃 | 及时释放资源、监控内存 |

---

## 📝 注意事项

1. **API Key 安全**: 使用 EncryptedSharedPreferences 存储敏感信息
2. **权限处理**: 动态申请 RECORD_AUDIO 权限
3. **后台限制**: 使用前台服务保持录音
4. **音频焦点**: 正确处理音频焦点切换
5. **错误处理**: 完善的异常捕获和用户提示
