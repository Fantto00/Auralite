# Vosk 离线语音识别模型使用指南

> 原文链接：https://cloud.baidu.com/article/3691891
> 整理时间：2026-05-22

## 一、下载模型

从 [官方模型仓库](https://alphacephei.com/vosk/models) 下载：

我下载了vosk-model-small-cn-0.22，并直接把zip压缩包复制到了assets目录下 名为vosk-model-small-cn.zip。后面自己写解压逻辑复制到本地文件夹。而不是像下面代码那样直接复制。

## 二、核心代码实现

### 2.1 初始化识别器

```java
import org.vosk.Model;
import org.vosk.Recognizer;

public class SpeechRecognizer {
    private Model model;
    private Recognizer recognizer;

    public void init(Context context) throws IOException {
        // 从 assets 复制模型到应用目录
        File modelDir = new File(context.getFilesDir(), "model");
        if (!modelDir.exists()) {
            try (InputStream in = context.getAssets().open("model");
                 OutputStream out = new FileOutputStream(modelDir)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        }
        model = new Model(modelDir.getAbsolutePath());
        recognizer = new Recognizer(model, 16000); // 采样率 16kHz
    }
}
```

**关键点**：
- 模型需从 `assets` 复制到应用私有目录（`getFilesDir()`）
- 采样率需与录音配置一致（通常为 16kHz）

### 2.2 录音与实时识别

```java
public class AudioCapture {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;

    public void startRecording(Recognizer recognizer) {
        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
        audioRecord.startRecording();

        byte[] buffer = new byte[bufferSize];
        while (true) {
            int bytesRead = audioRecord.read(buffer, 0, buffer.length);
            if (bytesRead > 0 && recognizer.acceptWaveForm(buffer, bytesRead)) {
                String result = recognizer.getResult();
                if (result != null) {
                    Log.d("Vosk", "识别结果: " + result);
                }
            }
        }
    }

    public void stopRecording() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
    }
}
```

### 2.3 获取识别结果

Vosk 提供两种结果获取方式：

| 方法 | 用途 | 适用场景 |
|------|------|----------|
| `recognizer.getPartialResult()` | 实时中间结果 | 实时显示识别内容 |
| `recognizer.getResult()` | 最终结果 | 命令触发、完整句子 |

## 三、常见问题与解决方案

### 3.1 模型加载失败

- **原因**：模型路径错误或文件损坏
- **解决**：检查 `modelDir` 路径，重新下载模型

### 3.2 性能优化

- **降低功耗**：在后台服务中运行识别，使用 `WakeLock` 防止休眠
- **内存管理**：及时释放不再使用的 `Recognizer` 和 `Model` 对象
- **线程分离**：使用线程分离录音与识别逻辑，避免阻塞 UI
- **缓冲区调整**：动态调整缓冲区大小（如 512 字节）以降低延迟



## 四、最佳实践总结

### 4.1 开发建议

1. **初始化时机**：在 Application 或 Service 中初始化，避免重复加载
2. **资源释放**：Activity/Service 销毁时及时释放 Model 和 Recognizer
3. **错误处理**：捕获 IOException，处理模型加载失败情况
4. **用户体验**：使用 `getPartialResult()` 提供实时反馈

### 4.2 性能优化清单

- [ ] 使用后台线程处理识别任务
- [ ] 合理设置缓冲区大小（512-4096 字节）
- [ ] 及时释放不用的 Recognizer 对象
- [ ] 避免频繁创建/销毁 Model 实例
- [ ] 使用 WakeLock 保持后台识别稳定性

## 五、参考资料

- Vosk 官方模型下载：https://alphacephei.com/vosk/models
- Vosk Android SDK：https://github.com/alphacep/vosk-android

---
*本文根据百度云开发者文章整理生成*
