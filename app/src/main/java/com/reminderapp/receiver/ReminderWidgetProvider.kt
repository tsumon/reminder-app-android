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
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

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
        val lunarText: String,
        val nextTitle: String?,
        val nextTimeText: String?,
        val completeReminderId: Long?
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
                        WidgetData(0, "", null, null, null)
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
        views.setTextViewText(R.id.widget_lunar, data.lunarText)

        if (data.nextTitle != null) {
            views.setTextViewText(R.id.widget_next_title, data.nextTitle)
            views.setTextViewText(R.id.widget_next_time, data.nextTimeText ?: "")
        } else {
            views.setTextViewText(R.id.widget_next_title, zh("暂无提醒"))
            views.setTextViewText(R.id.widget_next_time, "")
        }

        // 快捷完成按钮：只有存在「即将到来的提醒」时才显示
        val completeId = data.completeReminderId
        if (completeId != null) {
            val completeIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_COMPLETE
                putExtra(WidgetActionReceiver.EXTRA_REMINDER_ID, completeId)
            }
            val completePi = PendingIntent.getBroadcast(
                context,
                completeId.toInt() and 0x7FFFFFFF,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_complete_btn, completePi)
            views.setViewVisibility(R.id.widget_complete_btn, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_complete_btn, android.view.View.GONE)
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

                // 今天农历（系统历法，离线可用）
                val lunar = com.reminderapp.service.LunarCalendar.solarToLunar(now)
                val lunarText = lunar?.description?.let { zhf("农历 %s", it) } ?: ""

                // 下一次提醒：绝对时间 + 倒计时
                val nextTimeText = next?.let { r ->
                    val countdown = countdownText(r.nextTriggerAt - now)
                    "${fmt.format(Date(r.nextTriggerAt))} · $countdown"
                }

                WidgetData(
                    unhandledCount = unhandled,
                    lunarText = lunarText,
                    nextTitle = next?.title,
                    nextTimeText = nextTimeText,
                    completeReminderId = next?.id
                )
            }

        /** 生成倒计时文案：2小时15分后 / 3天2小时后 / 45天后 */
        private fun countdownText(diffMillis: Long): String {
            val minutes = diffMillis / 60000L
            if (minutes < 60) return zhf("%s分钟后", minutes.coerceAtLeast(0))
            val hours = minutes / 60
            if (hours < 24) return zhf("%1$s小时%2$s分后", hours, minutes % 60)
            val days = hours / 24
            return zhf("%1$s天%2$s小时后", days, hours % 24)
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
                    // 兜底：v1.8.7 小组件增强后 WidgetData 扩为 5 字段（lunarText 非空）
                    WidgetData(0, "", null, null, null)
                }
                withContext(Dispatchers.Main) {
                    val provider = ReminderWidgetProvider()
                    provider.applyData(appContext, manager, ids, data)
                }
            }
        }
    }
}
