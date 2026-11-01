package com.reminderapp.data
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.*
import com.reminderapp.service.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject
@RunWith(AndroidJUnit4::class) class SyncV4Test {
    @Test fun optionalProductionIosRoundtrip() = runBlocking {
        if(InstrumentationRegistry.getArguments().getString("roundtrip")!="true") return@runBlocking
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        try {
            val dir=java.io.File(context.getExternalFilesDir(null),"runtime-captures")
            val json=java.io.File(dir,"interop-ios.json").readText()
            val items=BackupService.importFromJson(json)!!
            val actions=ReminderActions(context,db,holidayRefresh={})
            actions.importItems(items,json)
            actions.importItems(items,json)
            val records=db.reminderRecordDao().getAll()
            assertEquals(records.map { it.syncId }.toSet().size,records.size)
            java.io.File(dir,"android-final-v4.json").writeText(BackupService.exportDatabase(db))
        } finally { db.close() }
    }
    @Test fun historyIsIdempotentUndoAndReminderDeletionSurviveStaleBackup() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        try {
            val sid="aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee"
            val rid="11111111-2222-4333-8444-555555555555"
            val json="""{"schemaVersion":4,"reminders":[{"syncId":"$sid","title":"History","firstTriggerAt":1800000000000,"nextTriggerAt":1800000000000,"records":[{"id":"$rid","performedAt":1799999900000,"type":"confirm","note":"Preserve completion source"}]}]}"""
            val actions=ReminderActions(context,db,holidayRefresh={})
            actions.importItems(BackupService.importFromJson(json)!!,json)
            actions.importItems(BackupService.importFromJson(json)!!,json)
            assertEquals(1,db.reminderRecordDao().getAll().size)
            assertEquals("Preserve completion source",db.reminderRecordDao().getAll().single().note)
            val parent=db.reminderDao().getAllSync().single()
            val exported=BackupService.exportDatabase(db)
            assertEquals(rid,JSONObject(exported).getJSONArray("reminders").getJSONObject(0).getJSONArray("records").getJSONObject(0).getString("id"))
            val undone=JSONObject(json).put("deletedRecordIds",org.json.JSONArray().put(rid)).toString()
            actions.importItems(BackupService.importFromJson(undone)!!,undone)
            actions.importItems(BackupService.importFromJson(json)!!,json)
            assertTrue(db.reminderRecordDao().getAll().isEmpty())
            val latest=db.reminderDao().getById(parent.id)!!
            assertTrue(actions.delete(latest))
            actions.importItems(BackupService.importFromJson(json)!!,json)
            assertTrue(db.reminderDao().getAllSync().isEmpty())
            assertTrue(JSONObject(BackupService.exportDatabase(db)).getJSONArray("deletedReminderIds").toString().contains(sid))
        } finally { db.close() }
    }
}
