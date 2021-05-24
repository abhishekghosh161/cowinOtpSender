package com.abhi.cowinotpsender

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CowinApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}