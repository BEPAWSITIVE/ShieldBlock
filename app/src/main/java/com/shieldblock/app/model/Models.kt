package com.shieldblock.app.model

data class AppItem(
    val name: String,
    val packageName: String,
    val isCustom: Boolean = false,
    var isBlocked: Boolean = false
)

data class BlockCategory(
    val id: String,
    val title: String,
    val description: String,
    val keywords: List<String>,
    val defaultDomains: List<String> = emptyList(),
    var isEnabled: Boolean = false
)

sealed class BlockReason {
    data class App(val appName: String, val packageName: String) : BlockReason()
    data class Domain(val domain: String, val url: String) : BlockReason()
    data class Keyword(val keyword: String, val source: String, val category: String? = null) : BlockReason()
    data class SettingsTamper(val details: String) : BlockReason()
    data class VpnBypass(val vpnPackage: String) : BlockReason()
}
