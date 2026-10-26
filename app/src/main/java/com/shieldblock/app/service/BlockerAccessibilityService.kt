package com.shieldblock.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.shieldblock.app.data.PreferencesManager
import com.shieldblock.app.model.BlockReason
import com.shieldblock.app.ui.BlockedOverlayActivity
import com.shieldblock.app.util.BrowserUrlExtractor
import com.shieldblock.app.util.PredefinedRules
import java.util.Locale

class BlockerAccessibilityService : AccessibilityService() {

    private lateinit var prefs: PreferencesManager
    private var lastBlockTimestamp = 0L
    private val debounceMs = 1500L

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager.getInstance(this)
        Log.d(TAG, "BlockerAccessibilityService created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "BlockerAccessibilityService connected")

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_DEFAULT or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        info.notificationTimeout = 50
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!prefs.isMasterProtectionEnabled) return

        val packageName = event.packageName?.toString() ?: return

        // 1. Whitelist our own application so user can configure rules
        if (packageName == applicationContext.packageName) return

        // 2. Direct App Blocking Check (e.g. YouTube, Instagram, etc.)
        if (prefs.isAppBlocked(packageName)) {
            val appName = getAppNameFromPackage(packageName)
            executeBlock(BlockReason.App(appName, packageName))
            return
        }

        // 3. Strict Mode: VPN App Bypass Check
        if (prefs.isBlockVpnEnabled && PredefinedRules.KNOWN_VPN_PACKAGES.contains(packageName)) {
            executeBlock(BlockReason.VpnBypass(packageName))
            return
        }

        // 4. Strict Mode: Anti-Tamper Android Settings Check
        if (prefs.isLockSettingsEnabled && packageName == "com.android.settings") {
            if (isSettingsTamperingAttempt(rootInActiveWindow)) {
                executeBlock(BlockReason.SettingsTamper("Attempted to modify accessibility or security settings"))
                return
            }
        }

        // 5. Browser URL & Content Inspection (Chrome, Firefox, Brave, Edge, Incognito sessions, etc.)
        if (PredefinedRules.KNOWN_BROWSERS.contains(packageName)) {
            inspectBrowser(rootInActiveWindow)
        }
    }

    private fun inspectBrowser(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return

        // 1. Extract URL bar text
        val url = BrowserUrlExtractor.extractUrl(rootNode)
        if (!url.isNullOrBlank()) {
            val cleanUrl = url.trim()

            // Check if domain is blocked (e.g. youtube.com, instagram.com)
            if (prefs.isDomainBlocked(cleanUrl)) {
                val host = BrowserUrlExtractor.extractHost(cleanUrl)
                executeBlock(BlockReason.Domain(host, cleanUrl))
                return
            }

            // Check if URL contains any blocked keywords (e.g. "shorts", "reels", custom keywords)
            val urlKwMatch = prefs.matchesKeyword(cleanUrl)
            if (urlKwMatch != null) {
                executeBlock(BlockReason.Keyword(urlKwMatch.first, "Browser Address", urlKwMatch.second))
                return
            }
        }

        // 2. Deep page inspection: check on-screen text for prohibited keywords and categories
        val visibleTexts = BrowserUrlExtractor.extractVisibleText(rootNode, maxNodes = 100)
        for (text in visibleTexts) {
            val match = prefs.matchesKeyword(text)
            if (match != null) {
                executeBlock(BlockReason.Keyword(match.first, "Webpage Content", match.second))
                return
            }
        }
    }

    private fun isSettingsTamperingAttempt(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false
        val screenTexts = BrowserUrlExtractor.extractVisibleText(rootNode, maxNodes = 60)
        val suspiciousKeywords = listOf(
            "shieldblock",
            "accessibility",
            "device admin",
            "force stop",
            "uninstall",
            "clear data",
            "special app access"
        )

        for (text in screenTexts) {
            val lower = text.lowercase(Locale.ROOT)
            for (kw in suspiciousKeywords) {
                if (lower.contains(kw)) {
                    return true
                }
            }
        }
        return false
    }

    private fun executeBlock(reason: BlockReason) {
        val now = System.currentTimeMillis()
        if (now - lastBlockTimestamp < debounceMs) {
            // Still in debounce window, dismiss window immediately
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }
        lastBlockTimestamp = now

        // 1. Send device to HOME screen to interrupt the prohibited application or page immediately
        performGlobalAction(GLOBAL_ACTION_HOME)

        // 2. Trigger haptic feedback
        vibrateWarning()

        // 3. Launch the full-screen Blocking Overlay
        val intent = Intent(this, BlockedOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

            when (reason) {
                is BlockReason.App -> {
                    putExtra(BlockedOverlayActivity.EXTRA_REASON_TYPE, "app")
                    putExtra(BlockedOverlayActivity.EXTRA_TITLE, reason.appName)
                    putExtra(BlockedOverlayActivity.EXTRA_DETAIL, "Application: ${reason.packageName}")
                }
                is BlockReason.Domain -> {
                    putExtra(BlockedOverlayActivity.EXTRA_REASON_TYPE, "domain")
                    putExtra(BlockedOverlayActivity.EXTRA_TITLE, reason.domain)
                    putExtra(BlockedOverlayActivity.EXTRA_DETAIL, "Restricted Website URL")
                }
                is BlockReason.Keyword -> {
                    putExtra(BlockedOverlayActivity.EXTRA_REASON_TYPE, "keyword")
                    putExtra(BlockedOverlayActivity.EXTRA_TITLE, reason.keyword)
                    val catStr = if (!reason.category.isNullOrEmpty()) "Category: ${reason.category}" else "Custom Keyword"
                    putExtra(BlockedOverlayActivity.EXTRA_DETAIL, "$catStr (Matched in ${reason.source})")
                }
                is BlockReason.SettingsTamper -> {
                    putExtra(BlockedOverlayActivity.EXTRA_REASON_TYPE, "tamper")
                    putExtra(BlockedOverlayActivity.EXTRA_TITLE, "Settings Protection")
                    putExtra(BlockedOverlayActivity.EXTRA_DETAIL, reason.details)
                }
                is BlockReason.VpnBypass -> {
                    putExtra(BlockedOverlayActivity.EXTRA_REASON_TYPE, "vpn")
                    putExtra(BlockedOverlayActivity.EXTRA_TITLE, "VPN Bypass Prohibited")
                    putExtra(BlockedOverlayActivity.EXTRA_DETAIL, "VPN Application: ${reason.vpnPackage}")
                }
            }
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start BlockedOverlayActivity", e)
        }
    }

    private fun vibrateWarning() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(200)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrate failed", e)
        }
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "BlockerAccessibilityService interrupted")
    }

    companion object {
        private const val TAG = "ShieldBlockService"
    }
}
