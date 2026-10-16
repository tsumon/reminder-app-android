package com.reminderapp.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/** Compatibility entry point for work persisted by older app versions; startup restores alarms. */
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
        val dao=com.reminderapp.data.database.AppDatabase.getInstance(applicationContext).reminderDao()
        dao.getById(inputData.getLong("reminder_id",0))?.let { ReminderScheduler(applicationContext).schedule(it) }
        Result.success()
    }
}
