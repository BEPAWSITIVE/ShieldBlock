package com.shieldblock.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shieldblock.app.R
import com.shieldblock.app.data.PreferencesManager
import com.shieldblock.app.ui.BlockedOverlayActivity
import com.shieldblock.app.ui.MainActivity
import com.shieldblock.app.util.PermissionHelper
import com.shieldblock.app.util.PredefinedRules

class ForegroundMonitorService : Service() {

    private lateinit var prefs: PreferencesManager
    private lateinit var usageStatsManager: UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var lastBlockedTimestamp = 0L
    private val debounceMs = 1500L

    companion object {
        private const val TAG = "ForegroundMonitor"
        private const val CHANNEL_ID = "shield_monitor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val CHECK_INTERVAL_MS = 300L

        fun start(context: Context) {
            val intent = Intent(context, ForegroundMonitorService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start ForegroundMonitorService", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager.getInstance(this)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            startMonitoring()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ShieldBlock Active Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps ShieldBlock active to monitor and block restricted applications."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShieldBlock Protection Active")
            .setContentText("Monitoring applications & digital wellbeing")
            .setSmallIcon(R.drawable.ic_shield)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startMonitoring() {
        isRunning = true
        handler.post(monitorRunnable)
    }

    private val monitorRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            try {
                checkForegroundApp()
            } catch (e: Exception) {
                Log.e(TAG, "Error in checkForegroundApp", e)
            }

            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    private fun checkForegroundApp() {
        if (!prefs.isMasterProtectionEnabled) return
        if (!PermissionHelper.hasUsageStatsPermission(this)) return

        val foregroundPkg = getForegroundPackage() ?: return
        if (foregroundPkg == packageName) return

        // 1. Check if app is in blocked list (e.g. YouTube com.google.android.youtube)
        if (prefs.isAppBlocked(foregroundPkg)) {
            blockApp(foregroundPkg, "app")
            return
        }

        // 2. Strict Mode: Check VPN apps
        if (prefs.isBlockVpnEnabled && PredefinedRules.KNOWN_VPN_PACKAGES.contains(foregroundPkg)) {
            blockApp(foregroundPkg, "vpn")
            return
        }
    }

    private fun getForegroundPackage(): String? {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 3000
        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime) ?: return null

        var lastEventPackage: String? = null
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                lastEventPackage = event.packageName
            }
        }
        return lastEventPackage
    }

    private fun blockApp(pkgName: String, reasonType: String) {
        val now = System.currentTimeMillis()
        if (now - lastBlockedTimestamp < debounceMs) {
            // Already triggered recently, just make sure Home screen is active
            sendToHomeScreen()
            return
        }
        lastBlockedTimestamp = now

        // 1. Kick user out to Home Screen immediately
        sendToHomeScreen()

        // 2. Launch Blocked Overlay Activity
        val appName = getAppName(pkgName)
        val overlayIntent = Intent(this, BlockedOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(BlockedOverlayActivity.EXTRA_REASON_TYPE, reasonType)
            putExtra(BlockedOverlayActivity.EXTRA_TITLE, appName)
            putExtra(BlockedOverlayActivity.EXTRA_DETAIL, "Application: $pkgName")
        }

        try {
            startActivity(overlayIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch BlockedOverlayActivity from ForegroundMonitor", e)
        }
    }

    private fun sendToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(homeIntent)
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(monitorRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
