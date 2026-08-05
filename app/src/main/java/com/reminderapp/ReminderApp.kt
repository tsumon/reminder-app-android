package com.reminderapp

import android.app.Application
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderScheduler

class ReminderApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var scheduler: ReminderScheduler
        private set

    lateinit var notificationManager: NotificationManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        scheduler = ReminderScheduler(this)
        notificationManager = NotificationManager(this)
    }

    companion object {
        lateinit var instance: ReminderApp
            private set
    }
}
