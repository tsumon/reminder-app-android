package com.reminderapp.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.reminderapp.MainActivity
import com.reminderapp.R
import com.reminderapp.data.database.AppDatabase
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

/**
 * 桌面小组件：显示未处理提醒数 + 距离最近的提醒
 * 数据由 App 在提醒变化时通过 [refresh] 主动刷新
 */
class ReminderWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = buildViews(context)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        refresh(context)
    }

    private fun buildViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_reminder)

        // 点击打开 App
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_unhandled_title, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_next_label, pendingIntent)

        // 查询数据
        val reminders = runBlocking {
            AppDatabase.getInstance(context).reminderDao().getAllSync()
        }
        val now = System.currentTimeMillis()

        // 未处理：notifying 状态或已到时间未确认
        val unhandled = reminders.filter {
            it.status == "notifying" ||
                (it.status in listOf("idle", "pending") && it.nextTriggerAt <= now)
        }
        views.setTextViewText(R.id.widget_unhandled_count, unhandled.size.toString())

        // 最近的未来提醒
        val next = reminders
            .filter { it.nextTriggerAt > now }
            .minByOrNull { it.nextTriggerAt }

        if (next != null) {
            views.setTextViewText(R.id.widget_next_title, next.title)
            val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            views.setTextViewText(R.id.widget_next_time, fmt.format(Date(next.nextTriggerAt)))
        } else {
            views.setTextViewText(R.id.widget_next_title, "暂无提醒")
            views.setTextViewText(R.id.widget_next_time, "")
        }

        return views
    }

    companion object {
        /** App 数据变化后调用，刷新所有已添加的小组件 */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ReminderWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return
            val provider = ReminderWidgetProvider()
            provider.onUpdate(context, manager, ids)
        }
    }
}
