package com.shieldblock.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.shieldblock.app.data.PreferencesManager
import com.shieldblock.app.service.ForegroundMonitorService
import com.shieldblock.app.util.PermissionHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Log.d("ShieldBlock", "Boot completed or package replaced: ShieldBlock active")
            val prefs = PreferencesManager.getInstance(context)
            if (prefs.isMasterProtectionEnabled && PermissionHelper.hasUsageStatsPermission(context)) {
                ForegroundMonitorService.start(context)
            }
        }
    }
}
