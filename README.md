# 📱 提醒助手 (Reminder App) — Android

一个功能完善的 Android 原生提醒应用，支持循环提醒、日期提醒、农历生日、节假日提醒以及 **AI 语音助手**。

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## ✨ 功能

### 1. 循环提醒
- 支持「每隔 N 分钟/小时/天/周/月/年」的周期提醒
- **锚点法计算**：基于首次触发时间，避免日期漂移
- **递增轰炸**：未确认 → 1h → 4h → 12h → 每天，确保不会错过
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
| **免 API 模式** | 零配置，自动复制文字 → 跳转豆包/千问/DeepSeek 网页版 |
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
│   ├── entity/          # Room 实体 (ReminderEntity)
│   ├── dao/             # DAO 接口
│   └── database/        # AppDatabase
├── service/
│   ├── ReminderEngine.kt    # 核心提醒引擎
│   ├── LunarCalendar.kt     # 农历转换 (1900-2100)
│   ├── HolidayService.kt    # 节假日服务
│   ├── AIService.kt         # AI API 调用 + Function Calling
│   ├── AITools.kt           # AI 工具定义
│   ├── AISettings.kt        # AI 配置存储
│   ├── VoiceService.kt      # 语音识别
│   └── NotificationHelper.kt # 通知管理
├── ui/screen/
│   ├── ReminderListScreen.kt    # 首页提醒列表
│   ├── CreateReminderScreen.kt  # 创建提醒
│   ├── EditReminderScreen.kt    # 编辑提醒
│   ├── AIChatScreen.kt          # AI 对话
│   └── AISettingsScreen.kt      # AI 设置
├── receiver/
│   ├── NotificationActionReceiver.kt  # 通知操作
│   └── BootReceiver.kt               # 开机重注册
├── worker/
│   └── ReminderWorker.kt   # WorkManager 后台任务
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
| v1.3.0 | 2026-08 | 免 API 模式：支持跳转豆包/千问/DeepSeek 网页版 |
| v1.2.0 | 2026-08 | AI 语音助手 + Function Calling |
| v1.1.0 | 2026-08 | 日期提醒、农历生日、节假日 |
| v1.0.0 | 2026-08 | 初始版：循环提醒 + 递增轰炸 |

---

## 📄 License

MIT
