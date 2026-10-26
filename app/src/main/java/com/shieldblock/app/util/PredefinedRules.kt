package com.shieldblock.app.util

import com.shieldblock.app.model.AppItem
import com.shieldblock.app.model.BlockCategory

object PredefinedRules {

    val DEFAULT_APPS = listOf(
        AppItem(name = "YouTube", packageName = "com.google.android.youtube", isBlocked = true),
        AppItem(name = "YouTube Kids", packageName = "com.google.android.apps.youtube.kids", isBlocked = true),
        AppItem(name = "YouTube Music", packageName = "com.google.android.apps.youtube.music", isBlocked = false),
        AppItem(name = "Instagram", packageName = "com.instagram.android", isBlocked = true),
        AppItem(name = "TikTok", packageName = "com.zhiliaoapp.musically", isBlocked = true),
        AppItem(name = "TikTok Lite", packageName = "com.ss.android.ugc.trill", isBlocked = true),
        AppItem(name = "Facebook", packageName = "com.facebook.katana", isBlocked = false),
        AppItem(name = "Twitter / X", packageName = "com.twitter.android", isBlocked = false),
        AppItem(name = "Reddit", packageName = "com.reddit.frontpage", isBlocked = false),
        AppItem(name = "Snapchat", packageName = "com.snapchat.android", isBlocked = false),
        AppItem(name = "Netflix", packageName = "com.netflix.mediaclient", isBlocked = false),
        AppItem(name = "Twitch", packageName = "tv.twitch.android.app", isBlocked = false),
        AppItem(name = "Discord", packageName = "com.discord", isBlocked = false),
        AppItem(name = "Telegram", packageName = "org.telegram.messenger", isBlocked = false),
        AppItem(name = "Google Play Store", packageName = "com.android.vending", isBlocked = false)
    )

    val KNOWN_BROWSERS = setOf(
        "com.android.chrome",
        "com.chrome.beta",
        "com.chrome.dev",
        "com.chrome.canary",
        "org.mozilla.firefox",
        "org.mozilla.firefox_beta",
        "org.mozilla.fenix",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.opera.gx",
        "com.sec.android.app.sbrowser",
        "com.sec.android.app.sbrowser.beta",
        "com.duckduckgo.mobile.android",
        "com.vivaldi.browser",
        "com.mi.globalbrowser",
        "com.UCMobile.intl",
        "com.kiwibrowser.browser",
        "org.torproject.torbrowser"
    )

    val KNOWN_VPN_PACKAGES = setOf(
        "com.cloudflare.onedotonedotonedotone",
        "com.wireguard.android",
        "de.blinkt.openvpn",
        "net.openvpn.openvpn",
        "com.nordvpn.android",
        "ch.protonvpn.android",
        "com.expressvpn.vpn",
        "free.vpn.unblock.proxy.turbovpn",
        "com.fast.free.unblock.thunder.vpn",
        "com.surfshark.vpnclient.android",
        "com.windscribe.vpn",
        "com.tunnelbear.android",
        "com.psiphon3.subscription"
    )

    val DEFAULT_DOMAINS = setOf(
        "youtube.com",
        "m.youtube.com",
        "youtu.be",
        "instagram.com",
        "m.instagram.com",
        "tiktok.com",
        "x.com",
        "twitter.com",
        "facebook.com",
        "m.facebook.com"
    )

    val PRESET_CATEGORIES = listOf(
        BlockCategory(
            id = "adult",
            title = "Adult / NSFW Content",
            description = "Blocks adult websites, explicit searches, and erotic media across all browsers.",
            keywords = listOf(
                "porn", "xxx", "sex", "nsfw", "adult", "xvideos", "pornhub", "xnxx",
                "redtube", "youporn", "erotic", "nude", "nudity", "hentai", "rule34",
                "onlyfans", "chaturbate", "stripchat", "cam4", "camgirl", "livejasmin"
            ),
            defaultDomains = listOf(
                "pornhub.com", "xvideos.com", "xnxx.com", "redtube.com",
                "youporn.com", "chaturbate.com", "onlyfans.com", "stripchat.com"
            ),
            isEnabled = true
        ),
        BlockCategory(
            id = "shorts",
            title = "Short-form Video & Reels",
            description = "Blocks addictive short-form video feeds, shorts, reels, and stories.",
            keywords = listOf(
                "shorts", "reels", "tiktok", "short video", "short videos",
                "spotlight", "stories", "viral shorts"
            ),
            defaultDomains = listOf(
                "tiktok.com", "kwai.com"
            ),
            isEnabled = true
        ),
        BlockCategory(
            id = "gambling",
            title = "Gambling & Betting",
            description = "Blocks online casinos, sports betting, poker, lottery, and crypto gambling.",
            keywords = listOf(
                "bet", "betting", "casino", "poker", "slots", "roulette", "blackjack",
                "lottery", "lotto", "jackpot", "stake.com", "1xbet", "bet365", "rollbit",
                "aviator game", "crash game", "gambling", "bookmaker"
            ),
            defaultDomains = listOf(
                "bet365.com", "stake.com", "1xbet.com", "bovada.lv", "draftkings.com", "fanduel.com"
            ),
            isEnabled = false
        ),
        BlockCategory(
            id = "social",
            title = "Social Media Networks",
            description = "Blocks major social media platforms and infinite-scroll feeds.",
            keywords = listOf(
                "instagram", "facebook", "tiktok", "twitter", "reddit", "snapchat",
                "threads.net", "pinterest", "feed", "timeline"
            ),
            defaultDomains = listOf(
                "instagram.com", "facebook.com", "twitter.com", "x.com", "reddit.com", "snapchat.com", "threads.net"
            ),
            isEnabled = false
        ),
        BlockCategory(
            id = "gaming",
            title = "Gaming & Live Streams",
            description = "Blocks game streaming sites, cloud gaming, and gaming portals.",
            keywords = listOf(
                "twitch", "kick.com", "roblox", "steamcommunity", "epicgames", "discord.com"
            ),
            defaultDomains = listOf(
                "twitch.tv", "kick.com", "roblox.com", "store.steampowered.com"
            ),
            isEnabled = false
        )
    )
}
