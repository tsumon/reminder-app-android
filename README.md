[🇨🇳 简体中文](README.md) · [🇺🇸 English](README.en.md) · [🇹🇼 繁體中文](README.zh-TW.md) · [🇯🇵 日本語](README.ja.md) · [🇰🇷 한국어](README.ko.md)

---

# 📱 提醒助手 (Reminder App) — Android

一个功能完善的 Android 原生提醒应用，支持循环提醒、日期提醒、农历生日、节假日提醒以及 **AI 语音助手**。

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

![App UI](docs/screenshots/android.png)

---

## 🌐 多语言支持 / Multi-language Support

本应用（Android 与 iOS 双端）内置多语言支持，跟随系统语言自动切换。

**Supported languages:**
- 🇨🇳 简体中文（zh-Hans）— 默认语言 / 回退语言 (default & fallback)
- 🇺🇸 English (en)
- 🇹🇼 繁體中文 (zh-Hant)
- 🇯🇵 日本語 (ja)
- 🇰🇷 한국어 (ko)

**实现方式 / Implementation:**
- **Android**：`res/values/strings.xml`（简体中文基准）+ `values-en` / `values-zh-rTW` / `values-ja` / `values-ko` 资源限定符；运行时代码统一通过 `zh()` / `zhf()`（`com.reminderapp.i18n`）查表，Service / Receiver 等非 Compose 场景亦可调用。
- **iOS**：`Localizable.xcstrings` 多语言目录，SwiftUI 通过 `String(localized:)` 取多语言文案。
- 两端共用「中文原串即 key」方案，新增 / 修改文案只需维护中文源串与译文表，降低重复命名成本。

> 多语言文案已覆盖全部用户可见界面（约 330 条），翻译缺失时自动回退为简体中文原串。
> All user-visible strings (~330) are localized; missing translations gracefully fall back to Simplified Chinese.

---

## ✨ 功能

### 1. 循环提醒
- 支持「每隔 N 分钟/小时/天/周/月/年」的周期提醒
- **锚点法计算**：基于首次触发时间，避免日期漂移（月末对齐、闰年正确）
- **递增重试**（对齐 iOS）：到点未确认 → 1h → 4h → 12h → 24h → 24h，达到上限标 `overdue` 停止轰炸，等用户手动处理
- 可暂停/恢复/删除

### 2. 日期提醒
- 一次性日期提醒：设定具体日期，当天准时通知
- **提前预告**：提前 N 天每日 9:00 推送预告通知
- 支持农历生日、阳历生日
- 内置 13 个中国法定节假日

### 3. AI 语音助手 🤖
- 自然语言创建提醒：「每天晚上 8 点提醒我遛狗」
- 支持 3 种模式：

| 模式 | 说明 |
|------|------|
| **免费 API** | 注册获取免费 Key，填入设置即可（含注册指引） |
| **自定义 API** | 使用自己的 OpenAI 兼容 API |

- 语音输入 (`SpeechRecognizer`)
- Function Calling 自动解析创建/查询/删除/推迟提醒
- 内置 5 个 API 模板（OpenAI / DeepSeek / 千问 / 豆包 / 通用）

---

## 🏗️ 技术栈

| 类别 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 数据持久化 | Room Database |
| 后台调度 | WorkManager |
| 通知 | NotificationCompat + NotificationChannel |
| 网络 | OkHttp 4 + Gson |
| 语音 | Android SpeechRecognizer |
| 架构 | MVVM + Repository |

---

## 📁 项目结构

```
app/src/main/java/com/reminderapp/
├── data/
│   ├── entity/          # Room 实体 (ReminderEntity, ReminderRecordEntity)
│   ├── dao/             # DAO 接口
│   └── database/        # AppDatabase (含迁移)
├── service/
│   ├── ReminderEngine.kt        # 核心提醒引擎（周期/确认/递增重试/遗漏检查）
│   ├── ReminderWorker.kt        # WorkManager 后台任务
│   ├── ReminderScheduler.kt     # WorkManager 调度器
│   ├── NotificationManager.kt   # 通知发送与渠道管理
│   ├── LunarCalendar.kt         # 农历转换 (1900-2100)
│   ├── HolidayService.kt        # 节假日服务
│   ├── AIService.kt             # AI API 调用 + Function Calling
│   ├── AITools.kt               # AI 工具定义
│   ├── AISettings.kt            # AI 配置存储
│   ├── VoiceService.kt          # 语音识别
│   ├── BackupHelper.kt          # JSON 备份导入导出
│   ├── WebDavSync.kt            # 坚果云/通用 WebDAV 双向同步
│   ├── NearbyShareService.kt    # 局域网 TCP 互传（47823）
│   └── QrCodeUtils.kt           # 二维码生成与扫码
├── ui/
│   ├── screen/                  # HomeScreen / CalendarScreen / StatsScreen / AIChatScreen / NearbyShareScreen / Settings / ...
│   ├── theme/                   # 设计令牌（与 iOS 主题色一致）
│   └── viewmodel/               # HomeViewModel / ReminderDetailViewModel / ...
├── receiver/
│   ├── NotificationActionReceiver.kt  # 通知确认/稍后按钮处理
│   └── BootReceiver.kt               # 开机重注册 WorkManager 任务
├── widget/                      # 桌面小部件
├── MainActivity.kt
└── ReminderApp.kt
```

---

## 🚀 构建 & 运行

### 前提条件

- [Android Studio](https://developer.android.com/studio) Hedgehog (2023.1.1) 或更新版本
- Android SDK 34
- JDK 17

### 构建步骤

```bash
# 1. 克隆仓库
git clone https://github.com/tsumon/reminder-app-android.git
cd reminder-app-android

# 2. 用 Android Studio 打开项目
#    会自动下载 Gradle 和依赖

# 3. 构建 APK
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Release APK (需要签名配置)

# 4. 安装到设备
./gradlew installDebug
```

### 生成 Release APK

在 `app/build.gradle.kts` 中配置签名：

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

然后：

```bash
./gradlew assembleRelease
# APK 路径: app/build/outputs/apk/release/app-release.apk
```

---

## 📱 系统要求

- Android 8.0 (API 26) 及以上
- 通知权限 (Android 13+ 需要手动授权)
- 麦克风权限 (AI 语音功能)
- 网络权限 (AI 功能)

---

## 🔄 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v2.1.0 | 2026-08 | UI 现代化（Material You 动态取色 / 夜间主题启动无闪白）+ 图标统一（Material 图标替代 emoji）+ 统一稍后选项（15 分钟/1 小时/明天/自定义）+ 跨端协议 v2（syncId 同步不丢引用）+ AI 深色模式修复 |
| v2.0.21 | 2026-08 | 数据与通知一致性 + UI 适配 + 回归测试（导入去重统一、一次性确认不重排、overdue 不唤醒、Worker 可重试、备份协议补齐字段） |
| v2.0.18 | 2026-08 | 首页/详情/设置 UI 优化 + 新图标 + 记录字段扩展 |
| v2.0.12 | 2026-08 | AI 聊天修正 |
| v2.0.11 | 2026-08 | 农历引擎修正 |
| v2.0.10 | 2026-08 | 农历引擎清理 |
| v2.0.9 | 2026-08 | 构建修正 |
| v2.0.8 | 2026-08 | 农历引擎大升级（权威历法）+ 回归测试扩充 |
| v2.0.7 | 2026-08 | AI 设置项 |
| v2.0.6 | 2026-08 | 数据库迁移 + AI 工具 |
| v2.0.5 | 2026-08 | 首页清理 |
| v2.0.4 | 2026-08 | 语言手动切换（跟随系统为默认）+ 设置页 |
| v2.0.3 | 2026-08 | CI 构建重试 |
| v2.0.2 | 2026-08 | 小组件/调度/多语言修正 |
| v2.0.1 | 2026-08 | 构建修正 |
| v2.0.0 | 2026-08 | 全面多语言（简/繁/英/日/韩）+ 模型/备份/小组件适配 |
| v1.9.8.1 | 2026-08 | 构建修正 |
| v1.9.8 | 2026-08 | 首页/日历/详情设计图风格 UI + AI 聊天完善 |
| v1.9.7 | 2026-08 | Android 递增重试对齐 iOS（1h→4h→12h→24h→24h→overdue），UI 状态 overdue 高亮 |
| v1.9.6 | 2026-08 | 五轮审查 70 项修复 + 近场传输 + 删除 AI 免 API 模式 |
| v1.9.5 | 2026-08 | 检查更新改 `releases.atom` 防 API 限流 |
| v1.9.4 | 2026-08 | WebDAV 同步 404 → 自动 MKCOL 建目录 |
| v1.9.3 | 2026-08 | 设置页（版本号/检查更新/更新日志） |
| v1.9.2 | 2026-08 | 更新检查超时重试 + WebDAV 友好提示 |
| v1.9.1 | 2026-08 | AI 规则提醒 + 首页菜单「检查更新」 |
| v1.9.0 | 2026-08 | UI 优化（液态玻璃）+ 在线升级 + App 图标 |
| v1.8.7 | 2026-08 | 小组件增强 / 节假日联网 / 统计洞察 / .ics 导出 / 设计令牌 / 崩溃监控 |
| v1.3.0 | 2026-08 | AI 语音助手 + Function Calling |
| v1.2.0 | 2026-08 | 日期提醒、农历生日、节假日 |
| v1.0.0 | 2026-08 | 初始版：循环提醒 + 递增轰炸 |

---

## 📄 License

MIT
