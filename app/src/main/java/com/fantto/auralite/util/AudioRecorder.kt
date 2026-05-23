package com.fantto.auralite.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.elvishew.xlog.XLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

/** AudioRecorder类，负责处理音频录制的具体逻辑，包括权限检查、录音控制和数据流的发出 **/
class AudioRecorder(private val context: Context) {

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val BUFFER_SIZE = 4096
    }

    private var recorder: AudioRecord? = null

    fun startRecording(): Flow<ByteArray> = flow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            XLog.e("AudioRecorder：未授予RECORD_AUDIO权限")
            return@flow
        }

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(bufferSize, BUFFER_SIZE)
        )

        recorder?.startRecording()
        XLog.d("AudioRecorder：开始录音")

        val buffer = ByteArray(BUFFER_SIZE)
        // 在flow构建器中，isActive是FlowCollector的属性，但在flow { }块中直接使用isActive是不正确的。应该使用currentCoroutineContext().isActive
        // 来检查协程是否仍然活跃。
        while (currentCoroutineContext().isActive) {
            val read = recorder?.read(buffer, 0, BUFFER_SIZE) ?: 0
            if (read > 0) {
                emit(buffer.copyOf(read))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun stopRecording() {
        recorder?.stop()
        recorder?.release()
        recorder = null
        XLog.d("AudioRecorder：停止录音")
    }

    fun isRecording(): Boolean {
        return recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING
    }
}