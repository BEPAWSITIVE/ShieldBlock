package com.shieldblock.app.util

import android.view.accessibility.AccessibilityNodeInfo
import java.net.URI
import java.util.Locale

object BrowserUrlExtractor {

    private val URL_BAR_VIEW_IDS = listOf(
        "com.android.chrome:id/url_bar",
        "com.android.chrome:id/search_box_text",
        "com.microsoft.emmx:id/url_bar",
        "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
        "org.mozilla.firefox:id/url_bar_title",
        "com.sec.android.app.sbrowser:id/location_bar_edit_text",
        "com.opera.browser:id/url_field",
        "com.brave.browser:id/url_bar",
        "com.duckduckgo.mobile.android:id/omnibarTextInput",
        "com.vivaldi.browser:id/url_bar",
        "com.mi.globalbrowser:id/url"
    )

    /**
     * Extracts the URL or address bar input from the root accessibility node of a browser.
     */
    fun extractUrl(rootNode: AccessibilityNodeInfo?): String? {
        if (rootNode == null) return null

        // 1. Try finding by known view IDs
        for (viewId in URL_BAR_VIEW_IDS) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (!nodes.isNullOrEmpty()) {
                val node = nodes[0]
                val text = node.text?.toString()?.trim()
                if (!text.isNullOrEmpty()) {
                    return text
                }
            }
        }

        // 2. Fallback heuristic: search for editable text or nodes matching URL patterns
        return searchUrlHeuristic(rootNode, 0)
    }

    private fun searchUrlHeuristic(node: AccessibilityNodeInfo?, depth: Int): String? {
        if (node == null || depth > 8) return null

        val text = node.text?.toString()?.trim()
        if (!text.isNullOrEmpty()) {
            val lower = text.lowercase(Locale.ROOT)
            if (lower.startsWith("http://") || lower.startsWith("https://") ||
                lower.contains(".com") || lower.contains(".org") ||
                lower.contains(".net") || lower.contains(".io") ||
                lower.contains(".co") || lower.contains(".in")
            ) {
                return text
            }
        }

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i)
            val result = searchUrlHeuristic(child, depth + 1)
            if (result != null) return result
        }

        return null
    }

    /**
     * Extracts visible on-screen text from the browser view hierarchy to match against blocked keywords.
     */
    fun extractVisibleText(rootNode: AccessibilityNodeInfo?, maxNodes: Int = 150): List<String> {
        val collectedText = mutableListOf<String>()
        if (rootNode == null) return collectedText

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(rootNode)
        var count = 0

        while (queue.isNotEmpty() && count < maxNodes) {
            val current = queue.removeFirst()
            count++

            val text = current.text?.toString()?.trim()
            if (!text.isNullOrEmpty() && text.length > 2) {
                collectedText.add(text)
            }

            val contentDesc = current.contentDescription?.toString()?.trim()
            if (!contentDesc.isNullOrEmpty() && contentDesc.length > 2) {
                collectedText.add(contentDesc)
            }

            for (i in 0 until current.childCount) {
                val child = current.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }

        return collectedText
    }

    /**
     * Normalizes a URL string to its clean hostname/domain.
     * e.g., "https://m.youtube.com/watch?v=123" -> "m.youtube.com"
     */
    fun extractHost(rawUrl: String): String {
        var clean = rawUrl.trim().lowercase(Locale.ROOT)
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        return try {
            val uri = URI(clean)
            val host = uri.host ?: ""
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (e: Exception) {
            // Fallback string extraction
            clean.substringAfter("://").substringBefore("/").substringBefore("?").replace("www.", "")
        }
    }
}
