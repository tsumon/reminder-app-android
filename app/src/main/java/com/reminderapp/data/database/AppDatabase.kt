package com.reminderapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reminderapp.data.dao.ReminderDao
import com.reminderapp.data.dao.ReminderRecordDao
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity

@Database(
    entities = [ReminderEntity::class, ReminderRecordEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun reminderRecordDao(): ReminderRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v1.2.0(v2): 新增 reminder_hour / reminder_minute
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN reminder_hour INTEGER NOT NULL DEFAULT 9")
                db.execSQL("ALTER TABLE reminders ADD COLUMN reminder_minute INTEGER NOT NULL DEFAULT 0")
            }
        }

        // v1.5.0(v3): 新增规则提醒字段 + priority
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN rule_period TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN rule_week INTEGER")
                db.execSQL("ALTER TABLE reminders ADD COLUMN rule_weekday INTEGER")
                db.execSQL("ALTER TABLE reminders ADD COLUMN priority TEXT NOT NULL DEFAULT 'normal'")
            }
        }

        // Item 2(v4): 新增节假日前移备注列（触发被前移时写入说明，确认后清空）
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN holiday_adjust_note TEXT")
            }
        }

        // 批次3 功能5(v5): 新增关键提醒标记列
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN is_critical INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reminder_app.db"
                )
                    // v1.9.6 fix: 原来靠 fallbackToDestructiveMigration 兜底，
                    // 老用户(v1.1/v1.2/v1.5)升级会整库 DROP 重建 → 全部提醒清空。
                    // 补 Migration 后 schema 不匹配时明确报错，绝不静默清库。
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
