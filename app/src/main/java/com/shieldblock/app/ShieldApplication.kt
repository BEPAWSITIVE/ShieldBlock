package com.shieldblock.app

import android.app.Application
import com.shieldblock.app.data.PreferencesManager

class ShieldApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.getInstance(this)
    }
}
