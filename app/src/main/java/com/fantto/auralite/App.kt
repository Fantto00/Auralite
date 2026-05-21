package com.fantto.auralite

import android.app.Application
import com.fantto.auralite.di.AppModule

class App : Application() {

    lateinit var appModule: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appModule = AppModule(applicationContext)
    }

    // 提供全局 app 单例
    companion object {
        lateinit var instance: App
            private set
    }
}