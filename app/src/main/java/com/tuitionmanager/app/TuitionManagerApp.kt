package com.tuitionmanager.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TuitionManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Offline-first initialization
    }
}
