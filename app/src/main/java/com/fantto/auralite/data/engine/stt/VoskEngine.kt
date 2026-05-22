package com.fantto.auralite.data.engine.stt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.fantto.auralite.domain.engine.SttEngine
import com.fantto.auralite.util.VoskModelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer

/** 基于Vosk库实现的离线语音识别引擎，负责处理音频输入、识别结果的解析和状态管理 **/
class VoskEngine(
    private val context: Context
) : SttEngine {

    companion object {
        private const val TAG = "VoskEngine"
        // Vosk模型要求的采样率为16000Hz，但是参数要求为float类型，
        // 但是AudioRecord音频处理类要求采样率为整数类型，所以定义两个常量分别满足两者的要求
        private const val SAMPLE_RATE_INT = 16000
        private const val SAMPLE_RATE_FLOAT = 16000.0f
        private const val BUFFER_SIZE = 4096
    }

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private val _transcription = MutableSharedFlow<String>()
    private val _partialResult = MutableSharedFlow<String>()

    private var _isListening = false

    override suspend fun initialize() {
        if (model != null) return

        val modelPath = VoskModelManager.getModelPath(context)
        model = Model(modelPath)
        recognizer = Recognizer(model, SAMPLE_RATE_FLOAT)
        Log.d(TAG, "Vosk model initialized at: $modelPath")
    }

    override fun startListening() {
        if (_isListening) return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "RECORD_AUDIO permission not granted")
            return
        }

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_INT,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE_INT,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(bufferSize, BUFFER_SIZE)
        )

        audioRecord?.startRecording()
        _isListening = true

        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(BUFFER_SIZE)
            val rec = recognizer ?: return@launch

            while (isActive && _isListening) {
                val bytesRead = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                if (bytesRead > 0) {
                    if (rec.acceptWaveForm(buffer, bytesRead)) {
                        val result = rec.result
                        val text = parseResultText(result)
                        if (text.isNotEmpty()) {
                            _transcription.emit(text)
                        }
                    } else {
                        val partial = rec.partialResult
                        val text = parsePartialText(partial)
                        if (text.isNotEmpty()) {
                            _partialResult.emit(text)
                        }
                    }
                }
            }

            val finalResult = rec.finalResult
            val text = parseResultText(finalResult)
            if (text.isNotEmpty()) {
                _transcription.emit(text)
            }
        }

        Log.d(TAG, "Started listening")
    }

    override fun stopListening() {
        if (!_isListening) return

        _isListening = false
        recordingJob?.cancel()
        recordingJob = null

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        Log.d(TAG, "Stopped listening")
    }

    override fun observeTranscription(): Flow<String> = _transcription

    override fun observePartialResult(): Flow<String> = _partialResult

    override fun isListening(): Boolean = _isListening

    override fun release() {
        stopListening()
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
        Log.d(TAG, "Released resources")
    }

    private fun parseResultText(json: String): String {
        return try {
            JSONObject(json).optString("text", "")
        } catch (e: Exception) {
            ""
        }
    }

    private fun parsePartialText(json: String): String {
        return try {
            JSONObject(json).optString("partial", "")
        } catch (e: Exception) {
            ""
        }
    }
}