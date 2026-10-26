package com.shieldblock.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shieldblock.app.model.AppItem
import com.shieldblock.app.model.BlockCategory
import com.shieldblock.app.util.BrowserUrlExtractor
import com.shieldblock.app.util.PredefinedRules
import java.util.Locale

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "shield_block_preferences"
        private const val KEY_MASTER_PROTECTION = "master_protection"
        private const val KEY_BLOCKED_APPS = "blocked_apps"
        private const val KEY_CUSTOM_APPS = "custom_apps"
        private const val KEY_BLOCKED_DOMAINS = "blocked_domains"
        private const val KEY_BLOCKED_KEYWORDS = "blocked_keywords"
        private const val KEY_ACTIVE_CATEGORIES = "active_categories"
        private const val KEY_LOCK_SETTINGS = "lock_settings"
        private const val KEY_BLOCK_VPN = "block_vpn"
        private const val KEY_PIN_CODE = "pin_code"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }

    var isMasterProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_MASTER_PROTECTION, true)
        set(value) = prefs.edit().putBoolean(KEY_MASTER_PROTECTION, value).apply()

    var isLockSettingsEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_SETTINGS, true)
        set(value) = prefs.edit().putBoolean(KEY_LOCK_SETTINGS, value).apply()

    var isBlockVpnEnabled: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_VPN, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_VPN, value).apply()

    var pinCode: String?
        get() = prefs.getString(KEY_PIN_CODE, null)
        set(value) = prefs.edit().putString(KEY_PIN_CODE, value).apply()

    val isPinProtectionEnabled: Boolean
        get() = !pinCode.isNullOrEmpty()

    fun verifyPin(pin: String): Boolean {
        val current = pinCode
        return current.isNullOrEmpty() || current == pin
    }

    // --- Blocked Apps ---

    fun getBlockedApps(): Set<String> {
        val defaultSet = PredefinedRules.DEFAULT_APPS.filter { it.isBlocked }.map { it.packageName }.toSet()
        return prefs.getStringSet(KEY_BLOCKED_APPS, defaultSet) ?: defaultSet
    }

    fun setAppBlocked(packageName: String, blocked: Boolean) {
        val current = getBlockedApps().toMutableSet()
        if (blocked) {
            current.add(packageName)
        } else {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_BLOCKED_APPS, current).apply()
    }

    fun isAppBlocked(packageName: String): Boolean {
        if (!isMasterProtectionEnabled) return false
        return getBlockedApps().contains(packageName)
    }

    fun getCustomApps(): List<AppItem> {
        val json = prefs.getString(KEY_CUSTOM_APPS, null) ?: return emptyList()
        val type = object : TypeToken<List<AppItem>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addCustomApp(item: AppItem) {
        val list = getCustomApps().toMutableList()
        if (list.none { it.packageName == item.packageName }) {
            list.add(item)
            prefs.edit().putString(KEY_CUSTOM_APPS, gson.toJson(list)).apply()
            setAppBlocked(item.packageName, true)
        }
    }

    fun removeCustomApp(packageName: String) {
        val list = getCustomApps().filter { it.packageName != packageName }
        prefs.edit().putString(KEY_CUSTOM_APPS, gson.toJson(list)).apply()
        setAppBlocked(packageName, false)
    }

    // --- Categories ---

    fun getActiveCategories(): Set<String> {
        val defaultSet = PredefinedRules.PRESET_CATEGORIES.filter { it.isEnabled }.map { it.id }.toSet()
        return prefs.getStringSet(KEY_ACTIVE_CATEGORIES, defaultSet) ?: defaultSet
    }

    fun setCategoryEnabled(categoryId: String, enabled: Boolean) {
        val current = getActiveCategories().toMutableSet()
        if (enabled) {
            current.add(categoryId)
        } else {
            current.remove(categoryId)
        }
        prefs.edit().putStringSet(KEY_ACTIVE_CATEGORIES, current).apply()
    }

    fun getCategories(): List<BlockCategory> {
        val active = getActiveCategories()
        return PredefinedRules.PRESET_CATEGORIES.map { cat ->
            cat.copy(isEnabled = active.contains(cat.id))
        }
    }

    // --- Blocked Domains ---

    fun getBlockedDomains(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKED_DOMAINS, PredefinedRules.DEFAULT_DOMAINS) ?: PredefinedRules.DEFAULT_DOMAINS
    }

    fun addBlockedDomain(domain: String) {
        val cleanDomain = BrowserUrlExtractor.extractHost(domain)
        if (cleanDomain.isNotEmpty()) {
            val current = getBlockedDomains().toMutableSet()
            current.add(cleanDomain)
            prefs.edit().putStringSet(KEY_BLOCKED_DOMAINS, current).apply()
        }
    }

    fun removeBlockedDomain(domain: String) {
        val current = getBlockedDomains().toMutableSet()
        current.remove(domain)
        prefs.edit().putStringSet(KEY_BLOCKED_DOMAINS, current).apply()
    }

    fun getAllEffectiveBlockedDomains(): Set<String> {
        val domains = getBlockedDomains().toMutableSet()
        val activeCatIds = getActiveCategories()
        for (cat in PredefinedRules.PRESET_CATEGORIES) {
            if (activeCatIds.contains(cat.id)) {
                domains.addAll(cat.defaultDomains)
            }
        }
        return domains
    }

    fun isDomainBlocked(urlOrDomain: String): Boolean {
        if (!isMasterProtectionEnabled) return false
        val host = BrowserUrlExtractor.extractHost(urlOrDomain)
        val allDomains = getAllEffectiveBlockedDomains()

        return allDomains.any { blocked ->
            host.equals(blocked, ignoreCase = true) ||
            host.endsWith(".$blocked", ignoreCase = true) ||
            urlOrDomain.contains(blocked, ignoreCase = true)
        }
    }

    // --- Blocked Keywords ---

    fun getCustomKeywords(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKED_KEYWORDS, emptySet()) ?: emptySet()
    }

    fun addBlockedKeyword(keyword: String) {
        val trimmed = keyword.trim().lowercase(Locale.ROOT)
        if (trimmed.isNotEmpty()) {
            val current = getCustomKeywords().toMutableSet()
            current.add(trimmed)
            prefs.edit().putStringSet(KEY_BLOCKED_KEYWORDS, current).apply()
        }
    }

    fun removeBlockedKeyword(keyword: String) {
        val current = getCustomKeywords().toMutableSet()
        current.remove(keyword.lowercase(Locale.ROOT))
        prefs.edit().putStringSet(KEY_BLOCKED_KEYWORDS, current).apply()
    }

    fun getAllEffectiveKeywords(): Map<String, String?> {
        val map = mutableMapOf<String, String?>()
        // Custom keywords
        getCustomKeywords().forEach { map[it] = "Custom Keyword" }

        // Category keywords
        val activeCatIds = getActiveCategories()
        for (cat in PredefinedRules.PRESET_CATEGORIES) {
            if (activeCatIds.contains(cat.id)) {
                for (kw in cat.keywords) {
                    map[kw.lowercase(Locale.ROOT)] = cat.title
                }
            }
        }
        return map
    }

    /**
     * Checks if a string contains any blocked keyword.
     * Returns Pair(keyword, categoryTitle) or null if no match.
     */
    fun matchesKeyword(text: String): Pair<String, String?>? {
        if (!isMasterProtectionEnabled || text.isBlank()) return null
        val lowerText = text.lowercase(Locale.ROOT)
        val allKeywords = getAllEffectiveKeywords()

        for ((kw, category) in allKeywords) {
            // Check boundary or substring
            if (lowerText.contains(kw)) {
                return Pair(kw, category)
            }
        }
        return null
    }
}
