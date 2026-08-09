[🇨🇳 简体中文](README.md) · [🇺🇸 English](README.en.md) · [🇹🇼 繁體中文](README.zh-TW.md) · [🇯🇵 日本語](README.ja.md) · [🇰🇷 한국어](README.ko.md)

---

# 📱 リマインダー (Reminder App) — Android

機能豊富な Android ネイティブのリマインダーアプリです。繰り返しリマインダー、日付リマインダー、旧暦の誕生日、祝日のリマインド、そして **AI 音声アシスタント** をサポートしています。

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

![App UI](docs/screenshots/android.png)

---

## 🌐 多言語対応 / Multi-language Support

本アプリ（Android と iOS の両方）は端末の言語設定に自動的に追従する多言語対応を内蔵しています。

**対応言語：**
- 🇨🇳 簡体字中国語（zh-Hans）— デフォルト & フォールバック
- 🇺🇸 English (en)
- 🇹🇼 繁体字中国語 (zh-Hant)
- 🇯🇵 日本語 (ja)
- 🇰🇷 한국어 (ko)

**実装 / Implementation：**
- **Android**：`res/values/strings.xml`（簡体字中国語ベース）+ `values-en` / `values-zh-rTW` / `values-ja` / `values-ko` のリソース修飾子。実行時は `zh()` / `zhf()`（`com.reminderapp.i18n`）を通じて文字列を検索し、`Service` / `Receiver` といった Compose 外のコンテキストからも呼び出せます。
- **iOS**：`Localizable.xcstrings` ローカリゼーションカタログ。SwiftUI は `String(localized:)` で多言語文字列を取得します。
- 両プラットフォームで「中国語の原文をキーとする」方式を共有しており、文字列の追加・変更は中国語の原文と翻訳表を管理するだけで済み、重複した命名コストを削減します。

> ユーザーに表示される文字列（約 330 件）はすべて多言語化されており、翻訳が欠落している場合は簡体字中国語の原文にフォールバックします。
> All user-visible strings (~330) are localized; missing translations gracefully fall back to Simplified Chinese.

---

## ✨ 機能

### 1. 繰り返しリマインダー
- 「N 分 / 時間 / 日 / 週 / 月 / 年 ごと」の周期的なリマインダーをサポート
- **アンカー方式の計算**：初回発火時刻を基準とし、日付のズレ（月末の調整やうるう年の処理）を防止
- **段階的再通知**（iOS と統一）：期限に未確認の場合 → 1時間 → 4時間 → 12時間 → 24時間 → 24時間。上限に達すると `overdue`（未完了）とマークして通知を止め、ユーザーの手動対応を待ちます
- 一時停止 / 再開 / 削除が可能

### 2. 日付リマインダー
- 一度きりの日付リマインダー：特定の日付を設定し、当日に正確に通知
- **事前お知らせ**：N 日前から毎日 9:00 にプレビュー通知を送信
- 旧暦の誕生日と新暦の誕生日をサポート
- 中国の法定祝日を 13 件内蔵

### 3. AI 音声アシスタント 🤖
- 自然言語でリマインダーを作成：「毎晩 8 時に犬の散歩をリマインドして」
- 3 つのモードをサポート：

| モード | 説明 |
|--------|------|
| **無料 API** | 登録して無料キーを取得し、設定に入力（登録手順付き） |
| **カスタム API** | 独自の OpenAI 互換 API を使用 |

- 音声入力（`SpeechRecognizer`）
- Function Calling で作成 / 検索 / 削除 / 延期を自動解析
- 5 つの組み込み API テンプレート（OpenAI / DeepSeek / Qwen / Doubao / 汎用）

---

## 🏗️ 技術スタック

| 区分 | 技術 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| データ永続化 | Room Database |
| バックグラウンド処理 | WorkManager |
| 通知 | NotificationCompat + NotificationChannel |
| ネットワーク | OkHttp 4 + Gson |
| 音声 | Android SpeechRecognizer |
| アーキテクチャ | MVVM + Repository |

---

## 📁 プロジェクト構成

```
app/src/main/java/com/reminderapp/
├── data/
│   ├── entity/          # Room エンティティ (ReminderEntity, ReminderRecordEntity)
│   ├── dao/             # DAO インターフェース
│   └── database/        # AppDatabase（マイグレーション付き）
├── service/
│   ├── ReminderEngine.kt        # コアリマインダーエンジン（周期 / 確認 / 段階的再通知 / 見落としチェック）
│   ├── ReminderWorker.kt        # WorkManager バックグラウンドタスク
│   ├── ReminderScheduler.kt     # WorkManager スケジューラ
│   ├── NotificationManager.kt   # 通知送信とチャンネル管理
│   ├── LunarCalendar.kt        # 旧暦変換 (1900-2100)
│   ├── HolidayService.kt       # 祝日サービス
│   ├── AIService.kt            # AI API 呼び出し + Function Calling
│   ├── AITools.kt              # AI ツール定義
│   ├── AISettings.kt           # AI 設定の保存
│   ├── VoiceService.kt         # 音声認識
│   ├── BackupHelper.kt         # JSON バックアップのインポート / エクスポート
│   ├── WebDavSync.kt           # Nutstore / 汎用 WebDAV 双方向同期
│   ├── NearbyShareService.kt   # 同じローカルネットワークの TCP 転送（47823）
│   └── QrCodeUtils.kt          # QR コード生成と読み取り
├── ui/
│   ├── screen/                  # HomeScreen / CalendarScreen / StatsScreen / AIChatScreen / NearbyShareScreen / Settings / ...
│   ├── theme/                   # デザイントークン（iOS のテーマカラーと統一）
│   └── viewmodel/               # HomeViewModel / ReminderDetailViewModel / ...
├── receiver/
│   ├── NotificationActionReceiver.kt  # 通知の「確認」「後で」ボタンの処理
│   └── BootReceiver.kt               # 起動時に WorkManager タスクを再登録
├── widget/                      # ホーム画面ウィジェット
├── MainActivity.kt
└── ReminderApp.kt
```

---

## 🚀 ビルドと実行

### 前提条件

- [Android Studio](https://developer.android.com/studio) Hedgehog (2023.1.1) 以降
- Android SDK 34
- JDK 17

### ビルド手順

```bash
# 1. リポジトリをクローン
git clone https://github.com/tsumon/reminder-app-android.git
cd reminder-app-android

# 2. Android Studio でプロジェクトを開く
#    Gradle と依存関係は自動的にダウンロードされます

# 3. APK をビルド
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Release APK（署名設定が必要）

# 4. 端末にインストール
./gradlew installDebug
```

### Release APK の生成

`app/build.gradle.kts` で署名を設定します：

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

その後：

```bash
./gradlew assembleRelease
# APK のパス: app/build/outputs/apk/release/app-release.apk
```

---

## 📱 システム要件

- Android 8.0 (API 26) 以降
- 通知権限（Android 13 以降は手動許可が必要）
- マイク権限（AI 音声機能）
- ネットワーク権限（AI 機能）

---

## 🔄 変更履歴

| バージョン | 日付 | 更新内容 |
|------------|------|----------|
| v1.9.7 | 2026-08 | Android の段階的再通知を iOS と統一（1h→4h→12h→24h→24h→overdue）、UI の `overdue` 状態を強調 |
| v1.9.6 | 2026-08 | 5 回のレビューで 70 件の修正 + 近距離転送 + AI 無料 API なしモードの削除 |
| v1.9.5 | 2026-08 | アップデート確認を `releases.atom` に変更（API レート制限を回避） |
| v1.9.4 | 2026-08 | WebDAV 同期の 404 → 自動 `MKCOL` でディレクトリ作成 |
| v1.9.3 | 2026-08 | 設定ページ（バージョン / アップデート確認 / 変更履歴） |
| v1.9.2 | 2026-08 | アップデート確認のタイムアウト再試行 + WebDAV の親切な案内 |
| v1.9.1 | 2026-08 | AI ルールリマインダー + ホームメニューの「アップデート確認」 |
| v1.9.0 | 2026-08 | UI 最適化（リキッドガラス）+ OTA アップデート + アプリアイコン |
| v1.8.7 | 2026-08 | ウィジェット強化 / 祝日のオンライン取得 / 統計インサイト / .ics 書き出し / デザイントークン / クラッシュ監視 |
| v1.3.0 | 2026-08 | AI 音声アシスタント + Function Calling |
| v1.2.0 | 2026-08 | 日付リマインダー、旧暦の誕生日、祝日 |
| v1.0.0 | 2026-08 | 初期版：繰り返しリマインダー + 段階的再通知 |

---

## 📄 ライセンス

MIT
