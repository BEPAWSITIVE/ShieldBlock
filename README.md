# 🛡️ ShieldBlock — Ultimate Android App, Website & Keyword Blocker

**ShieldBlock** is a high-enforcement Android digital focus and content restriction application. It is engineered to block distracting applications (like YouTube, TikTok, Instagram), prohibited websites/domains, and specific categories of web content or custom keywords directly at the Android OS View and Accessibility layer.

---

## ⚡ Key Capabilities & Why It Cannot Be Bypassed

- **🔒 App Blocker**: Blocks applications by package name (e.g. YouTube `com.google.android.youtube`, Instagram, TikTok, custom user apps). When opened, ShieldBlock instantly intercepts the window, closes the app, and displays a locked full-screen restriction overlay.
- **🌐 Web Domain Blocker**: Blocks specific websites (`youtube.com`, `instagram.com`, `reddit.com`, etc.) across all major Android browsers (Chrome, Firefox, Brave, Microsoft Edge, Opera, Samsung Internet, DuckDuckGo, etc.).
- **🔍 Real-Time Keyword & Category Blocker**:
  - Automatically filters content categories: **Adult / NSFW**, **Shorts & Reels**, **Gambling & Betting**, **Social Media**, and **Gaming**.
  - Allows adding **unlimited custom keywords** (e.g. `shorts`, `casino`, or any custom terms).
  - Inspects both the **browser URL / search query bar** and the **live on-screen rendered text nodes**.
- **🛡️ Unaffected by VPN or Incognito Mode**:
  - Traditional DNS-based blockers fail when a user turns on a VPN or switches to private DNS because traffic gets encrypted in a tunnel.
  - **ShieldBlock operates at the Android UI / Accessibility layer.** Even if a VPN is connected or the user is in an Incognito / Private browsing tab, the browser still renders the URL and screen text through Android's window manager. ShieldBlock detects the prohibited content on screen and kills the session immediately.
- **🚫 Anti-Tamper & Strict Mode**:
  - **Lock Android Settings**: Prevents opening phone settings to disable the Accessibility Service or force-stop the app.
  - **Block VPN Apps**: Prevents launching VPN apps to bypass network restrictions.
  - **Device Administrator**: Prevents normal uninstallation from the phone launcher or settings.
  - **PIN Protection**: Locks the ShieldBlock dashboard with a 4-6 digit PIN so block rules cannot be deleted on an impulse.

---

## 🚀 How to Build the APK with GitHub Actions

You don't need Android Studio or Java installed on your local computer. GitHub Actions will compile and generate the ready-to-install APK in the cloud.

### Step 1: Create a GitHub Repository
1. Go to [GitHub](https://github.com) and create a new repository (e.g., `ShieldBlock`).
2. Keep it **Private** or **Public**.

### Step 2: Push this Project to GitHub
Open your terminal in this directory (`C:\Users\Deepak\.gemini\antigravity\scratch\ShieldBlock`) and run:

```bash
git init
git add .
git commit -m "Initial commit of ShieldBlock app"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

### Step 3: Download the Generated APK
1. Open your repository on GitHub in your web browser.
2. Click on the **Actions** tab at the top.
3. You will see a workflow named **"Build ShieldBlock APK"** running automatically.
4. Once completed (approx. 2–3 minutes), click on the completed run.
5. Under the **Artifacts** section at the bottom of the page, click **`ShieldBlock-Debug-APK`** (or `ShieldBlock-Release-APK`) to download the `.zip` file containing the APK!

---

## 📱 Installation & Initial Setup on Your Phone

1. **Transfer the APK** to your Android phone (or download the artifact directly on your phone's browser).
2. Tap the APK file to install it. If prompted, enable **"Allow from this source"** in your phone's settings to allow sideloading.
3. Open **ShieldBlock**.
4. On the **Dashboard**, grant the required permissions:
   - **Accessibility Service (Required)**: Tap "Grant" -> Find "ShieldBlock" under Installed Services / Accessibility -> Turn it **ON**. *(This allows real-time app, URL, and screen keyword interception).*
   - **Display Over Other Apps (Required)**: Tap "Grant" -> Allow display overlay. *(This allows the locked blocking screen to appear instantly).*
   - **Usage Access (Recommended)**: Tap "Grant" -> Allow ShieldBlock.
5. In the **Strict Mode** tab (optional but recommended for maximum enforcement):
   - Turn on **Lock Android Settings** to prevent disabling the app.
   - Turn on **Block VPN Applications**.
   - Activate **Device Administrator** to prevent uninstallation.
   - Set a **PIN Code** to lock configuration changes.

---

## 🛠️ Project Structure

```
ShieldBlock/
├── .github/
│   └── workflows/
│       └── build.yml               # Automated APK CI workflow
├── app/
│   ├── build.gradle.kts            # App dependencies & SDK config
│   ├── src/main/
│   │   ├── AndroidManifest.xml     # App permissions, activities & services
│   │   ├── java/com/shieldblock/app/
│   │   │   ├── ShieldApplication.kt
│   │   │   ├── data/
│   │   │   │   └── PreferencesManager.kt       # Persistent rules & state
│   │   │   ├── model/
│   │   │   │   └── Models.kt                   # App, Category & Reason models
│   │   │   ├── receiver/
│   │   │   │   ├── BootReceiver.kt             # Auto-start on reboot
│   │   │   │   └── ShieldDeviceAdminReceiver.kt# Anti-uninstall lock
│   │   │   ├── service/
│   │   │   │   └── BlockerAccessibilityService.kt # Core URL & UI detection engine
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt             # Main navigation & dashboard
│   │   │   │   ├── DashboardFragment.kt       # Protection switch & status
│   │   │   │   ├── AppsFragment.kt            # App blocker management
│   │   │   │   ├── WebFragment.kt             # Domain & keyword management
│   │   │   │   ├── StrictFragment.kt          # Anti-bypass & PIN lock
│   │   │   │   ├── BlockedOverlayActivity.kt  # Fullscreen restriction screen
│   │   │   │   ├── AppsAdapter.kt
│   │   │   │   └── CategoriesAdapter.kt
│   │   │   └── util/
│   │   │       ├── BrowserUrlExtractor.kt     # Deep browser inspection helper
│   │   │       ├── PermissionHelper.kt        # System permission utilities
│   │   │       └── PredefinedRules.kt         # Pre-configured apps & categories
│   │   └── res/                               # Layouts, vector drawables & themes
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🔒 Content Categories Included by Default

| Category | Description | Sample Keywords Blocked |
| :--- | :--- | :--- |
| **Adult / NSFW** | Blocks explicit sites, pornography, and searches | `porn`, `xxx`, `sex`, `nsfw`, `pornhub`, `xvideos`, `hentai`, `erotic` |
| **Shorts & Reels** | Blocks short video addiction across apps & web | `shorts`, `reels`, `tiktok`, `spotlight`, `stories`, `short video` |
| **Gambling & Betting** | Blocks online casinos, sports betting & crash games | `casino`, `bet`, `poker`, `slots`, `jackpot`, `1xbet`, `stake`, `aviator` |
| **Social Media** | Blocks distracting social platforms and feeds | `instagram`, `facebook`, `tiktok`, `twitter`, `x.com`, `reddit`, `snapchat` |
| **Gaming & Streams** | Blocks gaming portals and streaming platforms | `twitch`, `kick.com`, `roblox`, `steamcommunity`, `epicgames` |

*You can also add any custom keywords, apps, or domains directly from the app's Web and Apps tabs.*
