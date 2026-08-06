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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 桌面小组件：显示未处理提醒数 + 距离最近的提醒
 * 数据由 App 在提醒变化时通过 [refresh] 主动刷新
 *
 * 注意：onUpdate 跑在广播的主线程上，绝对不能 runBlocking 查 Room，
 * 否则数据量一大 / 磁盘一慢就直接 ANR。这里统一走 IO 线程 + goAsync()。
 */
class ReminderWidgetProvider : AppWidgetProvider() {

    /** 小组件要展示的最小数据集，在 IO 线程算好再回主线程渲染 */
    private data class WidgetData(
        val unhandledCount: Int,
        val nextTitle: String?,
        val nextTimeText: String?
    )

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 广播回调必须在返回前保活，否则进程可能被杀导致更新丢失
        val pendingResult = goAsync()
        scope.launch {
            try {
                val data = loadData(context)
                withContext(Dispatchers.Main) {
                    applyData(context, appWidgetManager, appWidgetIds, data)
                }
            } catch (e: Exception) {
                // 查库失败也别让小组件卡在旧内容上，渲染一个空态兜底
                withContext(Dispatchers.Main) {
                    applyData(
                        context, appWidgetManager, appWidgetIds,
                        WidgetData(0, null, null)
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onEnabled(context: Context) {
        refresh(context)
    }

    private fun applyData(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray,
        data: WidgetData
    ) {
        for (id in ids) {
            manager.updateAppWidget(id, buildViews(context, data))
        }
    }

    /** 纯渲染，不碰数据库，可以安全跑在主线程 */
    private fun buildViews(context: Context, data: WidgetData): RemoteViews {
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

        views.setTextViewText(R.id.widget_unhandled_count, data.unhandledCount.toString())

        if (data.nextTitle != null) {
            views.setTextViewText(R.id.widget_next_title, data.nextTitle)
            views.setTextViewText(R.id.widget_next_time, data.nextTimeText ?: "")
        } else {
            views.setTextViewText(R.id.widget_next_title, "暂无提醒")
            views.setTextViewText(R.id.widget_next_time, "")
        }

        return views
    }

    companion object {
        /** 小组件是进程级单例式的，用一个共享 scope 承载后台查询 */
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private suspend fun loadData(context: Context): WidgetData =
            withContext(Dispatchers.IO) {
                val reminders = AppDatabase.getInstance(context).reminderDao().getAllSync()
                val now = System.currentTimeMillis()

                // 未处理：notifying 状态或已到时间未确认
                val unhandled = reminders.count {
                    it.status == "notifying" ||
                        (it.status in listOf("idle", "pending") && it.nextTriggerAt <= now)
                }

                val next = reminders
                    .filter { it.nextTriggerAt > now }
                    .minByOrNull { it.nextTriggerAt }

                val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                WidgetData(
                    unhandledCount = unhandled,
                    nextTitle = next?.title,
                    nextTimeText = next?.let { fmt.format(Date(it.nextTriggerAt)) }
                )
            }

        /** App 数据变化后调用，刷新所有已添加的小组件（异步，绝不阻塞调用方） */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ReminderWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return

            val appContext = context.applicationContext
            scope.launch {
                val data = try {
                    loadData(appContext)
                } catch (e: Exception) {
                    WidgetData(0, null, null)
                }
                withContext(Dispatchers.Main) {
                    val provider = ReminderWidgetProvider()
                    provider.applyData(appContext, manager, ids, data)
                }
            }
        }
    }
}
