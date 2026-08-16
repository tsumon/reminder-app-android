# Add project specific ProGuard rules here.

# 保留实体类（Room @Entity）
-keepattributes *Annotation*
-keep class com.reminderapp.data.entity.** { *; }

# 保留 Room 数据库与 DAO
# Room 在运行时用反射按名字找 AppDatabase_Impl / XxxDao_Impl，
# 一旦被 R8 改名或裁掉，App 启动就会 IllegalStateException: Cannot find implementation。
# 注意：DAO 声明是 interface，但生成的 XxxDao_Impl 是 class，
# 所以 interface / class 两种都要 keep，只写 interface 会漏掉实现类。
-keep class com.reminderapp.data.database.** { *; }
-keep class com.reminderapp.data.dao.** { *; }
-keep interface com.reminderapp.data.dao.** { *; }
-keep class **_Impl { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }

# Room 的泛型与内部实现
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * implements androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# 保留模型层（枚举/数据类，混淆会影响 JSON 序列化与 switch）
-keep class com.reminderapp.model.** { *; }

# 保留 Service 层对外接口符号（AIService / 工具调用反射用到的类名）
-keep class com.reminderapp.service.AIService { *; }
-keep class com.reminderapp.service.AITools { *; }

# v2.4.3: AI 对话历史 Gson 反射模型（字段名混淆后 JSON 键会变，跨版本读不回）
-keep class com.reminderapp.ui.screen.ChatMessage { *; }
-keep class com.reminderapp.ui.screen.ChatMessage$* { *; }
-keep class com.reminderapp.ui.screen.ChatHistoryStore { *; }
-keep class com.reminderapp.ui.screen.ToolStep { *; }
