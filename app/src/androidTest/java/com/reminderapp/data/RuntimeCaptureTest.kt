package com.reminderapp.data

import android.content.Intent
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Opt-in captures on a dedicated emulator only: no production demo data or main-source hooks. */
@RunWith(AndroidJUnit4::class)
class RuntimeCaptureTest {
    @Test fun captureRunningScreens() = runBlocking {
        val instrument=InstrumentationRegistry.getInstrumentation()
        if(InstrumentationRegistry.getArguments().getString("captureDemo")!="true") return@runBlocking
        val context=instrument.targetContext
        val db=AppDatabase.getInstance(context)
        val created=mutableListOf<Long>()
        val now=System.currentTimeMillis()
        fun clock(hour: Int)=java.util.Calendar.getInstance().apply { set(java.util.Calendar.HOUR_OF_DAY,hour);set(java.util.Calendar.MINUTE,0);set(java.util.Calendar.SECOND,0);set(java.util.Calendar.MILLISECOND,0) }.timeInMillis
        try {
            for((index,title) in listOf("提交周报","给绿植浇水","阅读半小时").withIndex()) {
                val first=if(index==0) now-86400000 else if(index==1) now-3600000 else now+6*3600000
                val item=ReminderEntity(title=title,cycle=if(index==2) "daily" else "weekly",firstTriggerAt=first,
                    nextTriggerAt=if(index==2) first else if(index==0) now-1800000 else now+3600000,status=if(index==2) "pending" else if(index==0) "overdue" else "notifying",
                    retryCount=if(index==2) 0 else if(index==0) 5 else 1,scheduledOccurrenceAt=if(index<2) first else null)
                val id=db.reminderDao().insert(item);created.add(id)
                for(day in 0..4) db.reminderRecordDao().insert(ReminderRecordEntity(reminderId=id,action="confirmed",timestamp=now-day*86400000))
                if(index==0) db.reminderRecordDao().insert(ReminderRecordEntity(reminderId=id,action="skipped",timestamp=now-60000))
            }
            val actions=com.reminderapp.service.ReminderActions(context,db,holidayRefresh={})
            val reversible=db.reminderDao().getById(created.last())!!
            val completion=actions.apply(reversible,com.reminderapp.service.ReminderActions.Action.COMPLETE,now=now)!!
            check(actions.undo(completion.undo!!))
            val discarded=ReminderEntity(title="Deleted interop fixture",firstTriggerAt=now+86400000,nextTriggerAt=now+86400000)
            val discardedId=db.reminderDao().insert(discarded);created.add(discardedId)
            check(actions.delete(discarded.copy(id=discardedId)))
            context.getSharedPreferences("locale_prefs",0).edit().putString("app_language","zh").commit()
            com.reminderapp.ui.theme.ThemeStore.setMode(context,if(InstrumentationRegistry.getArguments().getString("appearance")=="dark") 2 else 1)
            val intent=context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val scenario=androidx.test.core.app.ActivityScenario.launch<com.reminderapp.MainActivity>(intent)
            instrument.waitForIdleSync()
            fun hasText(node:android.view.accessibility.AccessibilityNodeInfo?,value:String):Boolean {
                if(node==null) return false
                if(node.text?.toString()?.contains(value)==true) return true
                return (0 until node.childCount).any { hasText(node.getChild(it),value) }
            }
            val deadline=android.os.SystemClock.uptimeMillis()+12000
            while(android.os.SystemClock.uptimeMillis()<deadline) {
                val root=instrument.uiAutomation.rootInActiveWindow
                if(hasText(root,"今天") && hasText(root,"全部")) break
                android.os.SystemClock.sleep(200)
            }
            check(hasText(instrument.uiAutomation.rootInActiveWindow,"全部")) {
                "Home content did not become accessible before capture"
            }
            android.os.SystemClock.sleep(350)
            val dir=File(context.getExternalFilesDir(null),"runtime-captures").apply { mkdirs() }
            fun capture(name:String) {
                val bitmap=instrument.uiAutomation.takeScreenshot()
                File(dir,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                bitmap.recycle()
            }
            fun tapTab(index:Int) {
                val screen=instrument.uiAutomation.takeScreenshot()
                val x=screen.width*(index*2+1)/8
                val density=context.resources.displayMetrics.density
                val y=screen.height-(54*density).toInt()
                screen.recycle()
                instrument.uiAutomation.executeShellCommand("input tap $x $y").close()
                android.os.SystemClock.sleep(1200)
            }
            File(dir,"android-v4.json").writeText(com.reminderapp.service.BackupService.exportDatabase(db))
            capture("home")
            tapTab(1);capture("calendar")
            tapTab(2);capture("stats")
            tapTab(3);capture("settings")
            scenario.close()
        } finally {
            created.forEach { db.reminderDao().deleteById(it) }
        }
    }
}
