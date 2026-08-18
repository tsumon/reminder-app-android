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
    version = 8,
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

        // v2.4.2(v7): weekly 意图星期（锚点错位检测/修正用）
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN weekly_weekday INTEGER")
            }
        }

        // v2.4.8(v8): 避开节假日/周末自动顺延（报税等工作日事务）
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN holiday_aware INTEGER NOT NULL DEFAULT 0")
            }
        }

        // 阶段2(v6): 跨端协议——sync_id（跨平台稳定 UUID）+ holiday_id（节假日稳定 ID）
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN sync_id TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN holiday_id TEXT")
                // 存量行补 UUID v4（SQLite 无 uuid()，用 randomblob 拼；sync_id 不允许再为 NULL，
                // 否则导出时该行无法被跨端识别，WebDAV 合并会重复插入）
                db.execSQL(
                    "UPDATE reminders SET sync_id = " +
                        "lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-' || " +
                        "'4' || substr(lower(hex(randomblob(2))), 2) || '-' || " +
                        "substr('89ab', abs(random()) % 4 + 1, 1) || substr(lower(hex(randomblob(2))), 2) || '-' || " +
                        "lower(hex(randomblob(6)))" +
                        " WHERE sync_id IS NULL"
                )
                // 部分唯一索引：NULL 不参与唯一性（旧数据已全量填充，防未来脏写）
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_reminders_sync_id ON reminders(sync_id) WHERE sync_id IS NOT NULL")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
