package com.fantto.auralite.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.elvishew.xlog.XLog
import com.fantto.auralite.App
import com.fantto.auralite.MainActivity
import com.fantto.auralite.R
import com.fantto.auralite.data.engine.stt.VoskEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 前台服务，负责持续运行语音识别引擎并提供结果的实时更新 **/
class VoiceRecognitionService : Service() {

    companion object {
        private const val CHANNEL_ID = "voice_recognition_channel"
        private const val NOTIFICATION_ID = 1

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _partialResult = MutableStateFlow("")
        val partialResult: StateFlow<String> = _partialResult.asStateFlow()

        private val _transcription = MutableStateFlow("")
        val transcription: StateFlow<String> = _transcription.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, VoiceRecognitionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VoiceRecognitionService::class.java)
            context.stopService(intent)
        }
    }

    private var voskEngine: VoskEngine? = null
    private var wakeLock: PowerManager.WakeLock? = null  //唤醒锁，防止设备休眠导致识别中断

    //自定义协程作用域
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        acquireWakeLock()
        startRecognition()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopRecognition()
        releaseWakeLock()
        serviceScope.cancel()
        _isRunning.value = false
        XLog.d("XLog VoiceRecognitionService : Service 结束")
    }

    // 启动语音识别引擎，并监听结果更新
    private fun startRecognition() {
        serviceScope.launch {
            try {
                val app = application as App
                voskEngine = VoskEngine(applicationContext)

                voskEngine?.initialize()
                voskEngine?.startListening()
                _isRunning.value = true

                launch {
                    voskEngine?.observePartialResult()?.collect { result ->
                        _partialResult.value = result
                    }
                }

                launch {
                    voskEngine?.observeTranscription()?.collect { result ->
                        _transcription.value = result
                    }
                }

                XLog.d("XLog VoiceRecognitionService : 开始识别")
            } catch (e: Exception) {
                XLog.e("XLog VoiceRecognitionService ：开启识别失败 ，异常：$e")
                stopSelf()
            }
        }
    }

    private fun stopRecognition() {
        voskEngine?.release()
        voskEngine = null
        _isRunning.value = false
        XLog.d("XLog VoiceRecognitionService : 停止识别")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "语音识别",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "语音识别服务运行中"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Auralite")
            .setContentText("语音识别服务运行中")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Auralite::VoiceRecognition"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 分钟超时
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
}