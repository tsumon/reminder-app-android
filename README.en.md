[🇨🇳 简体中文](README.md) · [🇺🇸 English](README.en.md) · [🇹🇼 繁體中文](README.zh-TW.md) · [🇯🇵 日本語](README.ja.md) · [🇰🇷 한국어](README.ko.md)

---

# 📱 Reminder Assistant (Reminder App) — Android

A full-featured native Android reminder app supporting recurring reminders, date reminders, lunar birthdays, holiday reminders, and an **AI voice assistant**.

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

![App UI](docs/screenshots/android.png)

---

## 🌐 Multi-language Support

This app (Android & iOS) has built-in multi-language support that follows the system language automatically.

**Supported languages:**
- 🇨🇳 Simplified Chinese (zh-Hans) — default & fallback
- 🇺🇸 English (en)
- 🇹🇼 Traditional Chinese (zh-Hant)
- 🇯🇵 Japanese (ja)
- 🇰🇷 Korean (ko)

**Implementation:**
- **Android**: `res/values/strings.xml` (zh-Hans base) + `values-en` / `values-zh-rTW` / `values-ja` / `values-ko` resource qualifiers; runtime code resolves strings through `zh()` / `zhf()` (`com.reminderapp.i18n`), which also works in non-Compose contexts such as `Service` / `Receiver`.
- **iOS**: `Localizable.xcstrings` localization catalog; SwiftUI retrieves strings via `String(localized:)`.
- Both platforms share the "Chinese source string as key" approach — adding or modifying a string only requires maintaining the Chinese source and the translation table, lowering duplication cost.

> All user-visible strings (~330) are localized; missing translations gracefully fall back to Simplified Chinese.

---

## ✨ Features

### 1. Recurring Reminders
- Supports "every N minutes / hours / days / weeks / months / years" periodic reminders
- **Anchor-based calculation**: based on the first trigger time, avoiding date drift (month-end alignment, correct leap years)
- **Escalating retry** (aligned with iOS): if not acknowledged at due time → 1h → 4h → 12h → 24h → 24h; once the cap is reached it is marked `overdue` and stops nagging, waiting for manual handling
- Can be paused / resumed / deleted

### 2. Date Reminders
- One-time date reminders: set a specific date and get notified on time that day
- **Early preview**: pushes a preview notification daily at 9:00 AM, N days in advance
- Supports lunar birthdays and solar birthdays
- 13 built-in Chinese statutory holidays

### 3. AI Voice Assistant 🤖
- Create reminders in natural language: "Remind me to walk the dog every evening at 8"
- Supports 3 modes:

| Mode | Description |
|------|------|
| **Free API** | Register to get a free key, then fill it into Settings (registration guide included) |
| **Custom API** | Use your own OpenAI-compatible API |

- Voice input (`SpeechRecognizer`)
- Function Calling automatically parses create / query / delete / postpone reminders
- 5 built-in API templates (OpenAI / DeepSeek / Qwen / Doubao / Generic)

---

## 🏗️ Tech Stack

| Category | Technology |
|----------|------------|
| UI | Jetpack Compose + Material 3 |
| Persistence | Room Database |
| Background scheduling | WorkManager |
| Notifications | NotificationCompat + NotificationChannel |
| Networking | OkHttp 4 + Gson |
| Voice | Android SpeechRecognizer |
| Architecture | MVVM + Repository |

---

## 📁 Project Structure

```
app/src/main/java/com/reminderapp/
├── data/
│   ├── entity/          # Room entities (ReminderEntity, ReminderRecordEntity)
│   ├── dao/             # DAO interfaces
│   └── database/        # AppDatabase (with migrations)
├── service/
│   ├── ReminderEngine.kt        # Core reminder engine (cycle / confirm / escalating retry / missed check)
│   ├── ReminderWorker.kt        # WorkManager background task
│   ├── ReminderScheduler.kt     # WorkManager scheduler
│   ├── NotificationManager.kt   # Notification sending & channel management
│   ├── LunarCalendar.kt        # Lunar calendar conversion (1900-2100)
│   ├── HolidayService.kt       # Holiday service
│   ├── AIService.kt            # AI API calls + Function Calling
│   ├── AITools.kt              # AI tool definitions
│   ├── AISettings.kt           # AI config storage
│   ├── VoiceService.kt         # Speech recognition
│   ├── BackupHelper.kt         # JSON backup import / export
│   ├── WebDavSync.kt           # Nutstore / generic WebDAV two-way sync
│   ├── NearbyShareService.kt   # LAN TCP transfer (port 47823)
│   └── QrCodeUtils.kt          # QR code generation & scanning
├── ui/
│   ├── screen/                  # HomeScreen / CalendarScreen / StatsScreen / AIChatScreen / NearbyShareScreen / Settings / ...
│   ├── theme/                   # Design tokens (matching iOS theme colors)
│   └── viewmodel/               # HomeViewModel / ReminderDetailViewModel / ...
├── receiver/
│   ├── NotificationActionReceiver.kt  # Notification confirm / later button handling
│   └── BootReceiver.kt               # Re-register WorkManager tasks on boot
├── widget/                      # Home screen widget
├── MainActivity.kt
└── ReminderApp.kt
```

---

## 🚀 Build & Run

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Hedgehog (2023.1.1) or newer
- Android SDK 34
- JDK 17

### Build Steps

```bash
# 1. Clone the repository
git clone https://github.com/tsumon/reminder-app-android.git
cd reminder-app-android

# 2. Open the project in Android Studio
#    Gradle and dependencies will be downloaded automatically

# 3. Build the APK
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Release APK (requires signing config)

# 4. Install to device
./gradlew installDebug
```

### Generate a Release APK

Configure signing in `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = "your_password"
            keyAlias = "your_alias"
            keyPassword = "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

Then:

```bash
./gradlew assembleRelease
# APK path: app/build/outputs/apk/release/app-release.apk
```

---

## 📱 System Requirements

- Android 8.0 (API 26) and above
- Notification permission (manual grant required on Android 13+)
- Microphone permission (AI voice feature)
- Network permission (AI feature)

---

## 🔄 Changelog

| Version | Date | Changes |
|---------|------|---------|
| v1.9.7 | 2026-08 | Android escalating retry aligned with iOS (1h→4h→12h→24h→24h→overdue), UI `overdue` state highlight |
| v1.9.6 | 2026-08 | Five review rounds with 70 fixes + nearby share + removed AI no-API mode |
| v1.9.5 | 2026-08 | Update check switched to `releases.atom` to avoid API rate limiting |
| v1.9.4 | 2026-08 | WebDAV sync 404 → auto `MKCOL` directory creation |
| v1.9.3 | 2026-08 | Settings page (version / check update / changelog) |
| v1.9.2 | 2026-08 | Update check timeout retry + friendly WebDAV hints |
| v1.9.1 | 2026-08 | AI rule reminders + home menu "Check Update" |
| v1.9.0 | 2026-08 | UI optimization (liquid glass) + OTA upgrade + app icon |
| v1.8.7 | 2026-08 | Widget enhancement / online holidays / stats insight / .ics export / design tokens / crash monitoring |
| v1.3.0 | 2026-08 | AI voice assistant + Function Calling |
| v1.2.0 | 2026-08 | Date reminders, lunar birthdays, holidays |
| v1.0.0 | 2026-08 | Initial: recurring reminders + escalating nag |

---

## 📄 License

MIT
