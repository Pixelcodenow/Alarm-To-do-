package com.example

import android.app.Application
import com.example.audio.ProceduralAudioEngine
import com.example.data.AppDatabase
import com.example.data.FocusAlarmRepository

class FocusAlarmApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val repository: FocusAlarmRepository by lazy {
        FocusAlarmRepository(database)
    }

    val audioEngine: ProceduralAudioEngine by lazy {
        ProceduralAudioEngine()
    }

    override fun onTerminate() {
        super.onTerminate()
        audioEngine.release()
    }
}
