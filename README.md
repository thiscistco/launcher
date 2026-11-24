# WorkOS Launcher — Productivity Super App

**For Infinix GT30 Pro** — Transforms Gaming vibe → Professional Productivity

### Wallpaper
- **Locked source:** `assets/wallpapers/default_wallpaper.jpg` v17 (kept unchanged)
- **Launcher default:** `assets/wallpapers/default_wallpaper_modern.png` (no labels or camera module)
- **Resolution:** 1440×3200 • same v17 aspect ratio, with the five vent slits retained for battery alignment
- **Design:** Cleaner graphite and polished titanium, crisp cyan circuitry, bright orange X; no rusty patina
- **Preview:** `index.html` + `preview/index.html` — GT30 Pro 6.78" with bezel & punch hole

### Tech Stack
- **Kotlin + Jetpack Compose** (modern, Google recommended 2025-26)
- **Min SDK 26 / Target 34 / Compile 34**
- **Launcher** (HOME intent) + **Super App** (Notes, Todo, Calendar, Focus, Files)

### Structure
```
assets/wallpapers/default_wallpaper.jpg  # v17 locked source (unchanged)
assets/wallpapers/default_wallpaper_modern.png  # modern label- and camera-free launcher image
app/src/main/res/drawable-nodpi/default_wallpaper_modern.jpg  # Android drawable
app/src/main/java/com/workos/launcher/
  MainActivity.kt      # Home with wallpaper + parallax option
  SuperAppActivity.kt  # All-in-One productivity
  SettingsActivity.kt  # Launcher settings
  ui/theme/Theme.kt
app/src/main/AndroidManifest.xml  # HOME launcher
```

### Build APK Release
> Requires Android SDK + JDK 17 locally (not available in this sandbox)

```bash
# In Android Studio or CLI
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
# For signed release, add keystore to app/build.gradle.kts signingConfigs
```

### Features
- **Home:** Focus Today widget, Super App quick access, universal search, app grid (productivity categorized), dock
- **Super App:** Notes, Todo, Calendar (Google Calendar sync via connected apps), Pomodoro/Habit, Files
- **Settings:** Parallax toggle, Hide Gaming Apps in Focus Mode
- **Wallpaper:** v17 edge-to-edge, optional parallax via gyroscope (toggle in Settings)

### Connected Apps
- `supabase` (for sync), `vercel` (for preview), `gmail`, `google_calendar`, `google_drive`, `google_docs`, `google_sheets`, `google_slides`

### Next Steps (per user request: build now, continue later)
- Implement full App Drawer with categories
- Implement Super App storage (Room + Supabase sync)
- Implement Parallax Live Wallpaper service
- Add Focus Mode (block social/gaming apps)

---
Branch: `arena/2a271a39-launcher` • Wallpaper locked v17 • Parallax option included
