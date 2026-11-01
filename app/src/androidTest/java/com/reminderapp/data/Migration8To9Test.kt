package com.reminderapp.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reminderapp.data.database.AppDatabase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking

/** Build a populated real v8 store, then let Room run and validate the actual additive migration. */
@RunWith(AndroidJUnit4::class)
class Migration8To9Test {
    @Test fun populatedV8PreservesEveryOldColumnAndRecords() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="migration-v8-v9.db"
        context.deleteDatabase(name)
        val old=context.openOrCreateDatabase(name,0,null)
        old.execSQL("""CREATE TABLE reminders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, kind TEXT NOT NULL, cycle TEXT NOT NULL, custom_days INTEGER NOT NULL, rule_period TEXT, rule_week INTEGER, rule_weekday INTEGER, date_type TEXT, target_month INTEGER, target_day INTEGER, holiday_name TEXT, holiday_id TEXT, advance_days INTEGER NOT NULL, reminder_hour INTEGER NOT NULL, reminder_minute INTEGER NOT NULL, title TEXT NOT NULL, note TEXT NOT NULL, priority TEXT NOT NULL, status TEXT NOT NULL, sync_id TEXT, weekly_weekday INTEGER, first_trigger_at INTEGER NOT NULL, next_trigger_at INTEGER NOT NULL, last_confirmed_at INTEGER, retry_count INTEGER NOT NULL, holiday_adjust_note TEXT, is_critical INTEGER NOT NULL, holiday_aware INTEGER NOT NULL, created_at INTEGER NOT NULL, is_active INTEGER NOT NULL)""")
        old.execSQL("CREATE UNIQUE INDEX idx_reminders_sync_id ON reminders(sync_id) WHERE sync_id IS NOT NULL")
        old.execSQL("""CREATE TABLE reminder_records (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, reminder_id INTEGER NOT NULL, action TEXT NOT NULL, timestamp INTEGER NOT NULL, FOREIGN KEY(reminder_id) REFERENCES reminders(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
        old.execSQL("""INSERT INTO reminders VALUES (42,'cycle','daily',3,'monthly',2,3,'lunar_birthday',8,15,'中秋','zhongqiu',3,11,30,'存量提醒','note','high','notifying','stable-42',5,1000,987654321,2000,2,'holiday',1,1,500,1)""")
        old.execSQL("INSERT INTO reminder_records VALUES(77,42,'confirmed',3000)")
        fun snapshot(query:String): List<List<String?>> = old.rawQuery(query,null).use { cursor -> buildList { while(cursor.moveToNext()) add((0 until cursor.columnCount).map { if(cursor.isNull(it)) null else cursor.getString(it) }) } }
        val before=snapshot("SELECT * FROM reminders")
        val columns=old.rawQuery("SELECT * FROM reminders",null).use { it.columnNames.joinToString(",") }
        old.version=8;old.close()
        val upgraded=Room.databaseBuilder(context,AppDatabase::class.java,name).addMigrations(AppDatabase.MIGRATION_8_9,AppDatabase.MIGRATION_9_10).build()
        try {
            val sql=upgraded.openHelper.writableDatabase // forces Room schema validation
            val after=sql.query("SELECT $columns FROM reminders").use { cursor -> buildList { while(cursor.moveToNext()) add((0 until cursor.columnCount).map { if(cursor.isNull(it)) null else cursor.getString(it) }) } }
            assertEquals(before,after)
            val r=upgraded.reminderDao().getById(42)!!
            assertEquals("stable-42",r.syncId);assertEquals(987654321L,r.nextTriggerAt);assertEquals(2,r.retryCount)
            assertEquals("fixed",r.recurrenceMode);assertEquals("important",r.reminderStyle);assertEquals(5,r.maxReminderAttempts)
            assertEquals("60,240,720,1440",r.retryIntervalsMinutes);assertNull(r.quietStartMinute)
            assertEquals(77L,upgraded.reminderRecordDao().getAll().single().id)
        } finally { upgraded.close();context.deleteDatabase(name) }
    }
    @Test fun build70To71_populatedV8PreservesEveryOldColumnAndRecords() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="migration-v9-v10.db"
        context.deleteDatabase(name)
        val old=context.openOrCreateDatabase(name,0,null)
        old.execSQL("""CREATE TABLE reminders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, kind TEXT NOT NULL, cycle TEXT NOT NULL, custom_days INTEGER NOT NULL, rule_period TEXT, rule_week INTEGER, rule_weekday INTEGER, date_type TEXT, target_month INTEGER, target_day INTEGER, holiday_name TEXT, holiday_id TEXT, advance_days INTEGER NOT NULL, reminder_hour INTEGER NOT NULL, reminder_minute INTEGER NOT NULL, title TEXT NOT NULL, note TEXT NOT NULL, priority TEXT NOT NULL, status TEXT NOT NULL, sync_id TEXT, weekly_weekday INTEGER, first_trigger_at INTEGER NOT NULL, next_trigger_at INTEGER NOT NULL, last_confirmed_at INTEGER, retry_count INTEGER NOT NULL, holiday_adjust_note TEXT, is_critical INTEGER NOT NULL, holiday_aware INTEGER NOT NULL, created_at INTEGER NOT NULL, is_active INTEGER NOT NULL)""")
        old.execSQL("CREATE UNIQUE INDEX idx_reminders_sync_id ON reminders(sync_id) WHERE sync_id IS NOT NULL")
        old.execSQL("""CREATE TABLE reminder_records (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, reminder_id INTEGER NOT NULL, action TEXT NOT NULL, timestamp INTEGER NOT NULL, FOREIGN KEY(reminder_id) REFERENCES reminders(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
        old.execSQL("""INSERT INTO reminders VALUES (42,'cycle','daily',3,'monthly',2,3,'lunar_birthday',8,15,'中秋','zhongqiu',3,11,30,'存量提醒','note','high','notifying','stable-42',5,1000,987654321,2000,2,'holiday',1,1,500,1)""")
        old.execSQL("INSERT INTO reminder_records VALUES(77,42,'confirmed',3000)")
        fun snapshot(query:String): List<List<String?>> = old.rawQuery(query,null).use { cursor -> buildList { while(cursor.moveToNext()) add((0 until cursor.columnCount).map { if(cursor.isNull(it)) null else cursor.getString(it) }) } }
        old.execSQL("ALTER TABLE reminders ADD COLUMN recurrence_mode TEXT NOT NULL DEFAULT 'fixed'")
        old.execSQL("ALTER TABLE reminders ADD COLUMN reminder_style TEXT NOT NULL DEFAULT 'persistent'")
        old.execSQL("ALTER TABLE reminders ADD COLUMN retry_intervals_minutes TEXT NOT NULL DEFAULT '60,240,720,1440'")
        old.execSQL("ALTER TABLE reminders ADD COLUMN max_reminder_attempts INTEGER NOT NULL DEFAULT 5")
        old.execSQL("ALTER TABLE reminders ADD COLUMN quiet_start_minute INTEGER")
        old.execSQL("ALTER TABLE reminders ADD COLUMN quiet_end_minute INTEGER")
        old.execSQL("ALTER TABLE reminders ADD COLUMN revision INTEGER NOT NULL DEFAULT 0")
        old.execSQL("UPDATE reminders SET recurrence_mode='afterCompletion',reminder_style='important',retry_intervals_minutes='15,45,180',max_reminder_attempts=7,quiet_start_minute=1320,quiet_end_minute=480,revision=42")
        val before=snapshot("SELECT * FROM reminders")
        val columns=old.rawQuery("SELECT * FROM reminders",null).use { it.columnNames.joinToString(",") }
        old.version=9;old.close()
        val upgraded=Room.databaseBuilder(context,AppDatabase::class.java,name).addMigrations(AppDatabase.MIGRATION_9_10).build()
        try {
            val sql=upgraded.openHelper.writableDatabase // forces Room schema validation
            val after=sql.query("SELECT $columns FROM reminders").use { cursor -> buildList { while(cursor.moveToNext()) add((0 until cursor.columnCount).map { if(cursor.isNull(it)) null else cursor.getString(it) }) } }
            assertEquals(before,after)
            val r=upgraded.reminderDao().getById(42)!!
            assertEquals("stable-42",r.syncId);assertEquals(987654321L,r.nextTriggerAt);assertEquals(2,r.retryCount)
            assertEquals("afterCompletion",r.recurrenceMode);assertEquals("important",r.reminderStyle);assertEquals(7,r.maxReminderAttempts)
            assertEquals("15,45,180",r.retryIntervalsMinutes);assertEquals(1320,r.quietStartMinute);assertEquals(480,r.quietEndMinute);assertEquals(42L,r.revision);assertNull(r.scheduledOccurrenceAt)
            val record=upgraded.reminderRecordDao().getAll().single()
            assertEquals(77L,record.id);assertEquals(3000L,record.timestamp);assertTrue(record.syncId.isNotBlank())
        } finally { upgraded.close();context.deleteDatabase(name) }
    }
}
