package com.example.camera

import android.app.Application
import android.content.Context

class CameraApp : Application() {

    companion object {
        private var instance: CameraApp? = null

        val appContext: Context? get() = instance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
