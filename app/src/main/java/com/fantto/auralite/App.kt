package com.fantto.auralite

import android.app.Application
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.fantto.auralite.di.AppModule

class App : Application() {

    lateinit var appModule: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appModule = AppModule(applicationContext)

        // 初始化 XLog
        XLog.init(LogLevel.ALL)
    }

    // 提供全局 app 单例
    companion object {
        lateinit var instance: App
            private set
    }
}