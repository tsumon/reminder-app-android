package com.reminderapp.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.comic.ComicPalettes
import com.reminderapp.ui.screen.SmartList
import java.util.Calendar

fun matchSmartList(reminder: ReminderEntity, list: SmartList): Boolean = when (list) {
    SmartList.ALL -> true
    SmartList.TODAY -> occursWithinDays(reminder, 0, 0)
    SmartList.TOMORROW -> occursWithinDays(reminder, 1, 1)
    SmartList.WEEK -> occursWithinDays(reminder, 0, 6)
    SmartList.MONTH -> occursWithinDays(reminder, 0, daysLeftInMonth())
    SmartList.HIGH -> reminder.priority == "high"
    SmartList.DONE -> reminder.status == "confirmed"
}

fun matchesSearch(reminder: ReminderEntity, query: String): Boolean {
    if (query.isBlank()) return true
    return reminder.title.contains(query, ignoreCase = true) ||
        reminder.note.contains(query, ignoreCase = true)
}

private fun occursWithinDays(reminder: ReminderEntity, fromDay: Int, toDay: Int): Boolean {
    if (toDay < fromDay) return false
    for (i in fromDay..toDay) {
        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, i) }
        if (
            ReminderEngine.occursOn(
                reminder,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH)
            )
        ) return true
    }
    return false
}

private fun daysLeftInMonth(): Int {
    val c = Calendar.getInstance()
    return c.getActualMaximum(Calendar.DAY_OF_MONTH) - c.get(Calendar.DAY_OF_MONTH)
}

@Composable
fun HomeFilterDropdown(
    expanded: Boolean,
    kindFilter: String,
    sortOrder: String,
    onKindChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        listOf(
            "all" to "全部类型",
            "cycle" to "周期提醒",
            "date" to "日期提醒",
            "rule" to "规则提醒"
        ).forEach { (id, label) ->
            DropdownMenuItem(
                text = {
                    Text(
                        (if (kindFilter == id) "✓ " else "") + zh(label),
                        fontWeight = if (kindFilter == id) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = { onKindChange(id); onDismiss() }
            )
        }
        HorizontalDivider()
        listOf(
            "time" to "按到期时间排列",
            "priority" to "优先级",
            "title" to "提醒标题"
        ).forEach { (id, label) ->
            DropdownMenuItem(
                text = {
                    Text(
                        (if (sortOrder == id) "✓ " else "") + zh(label),
                        fontWeight = if (sortOrder == id) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = { onSortChange(id); onDismiss() }
            )
        }
    }
}

@Composable
fun HomePaletteDropdown(
    expanded: Boolean,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        ComicPalettes.All.forEachIndexed { idx, spec ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(spec.accent)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            zh(spec.name),
                            fontWeight = if (activeIndex == idx) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                onClick = { onSelect(idx); onDismiss() }
            )
        }
    }
}

@Composable
fun HomeOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEnterSelection: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenSyncSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onImport: () -> Unit,
    onShowCardImport: () -> Unit,
    onExport: () -> Unit,
    onExportICS: () -> Unit,
    onNearbyShare: () -> Unit,
    onCheckUpdate: () -> Unit,
    onOpenSettings: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(zh("批量管理")) },
            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            onClick = { onDismiss(); onEnterSelection() }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(zh("立即同步")) },
            leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) },
            onClick = { onDismiss(); onSyncNow() }
        )
        DropdownMenuItem(
            text = { Text(zh("同步设置")) },
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = { onDismiss(); onOpenSyncSettings() }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(zh("统计洞察")) },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            onClick = { onDismiss(); onOpenStats() }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(zh("导入提醒")) },
            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
            onClick = { onDismiss(); onImport() }
        )
        DropdownMenuItem(
            text = { Text(zh("导入分享卡片")) },
            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
            onClick = { onDismiss(); onShowCardImport() }
        )
        DropdownMenuItem(
            text = { Text(zh("导出提醒")) },
            leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
            onClick = { onDismiss(); onExport() }
        )
        DropdownMenuItem(
            text = { Text(zh("导出日历(.ics)")) },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            onClick = { onDismiss(); onExportICS() }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(zh("附近传输")) },
            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
            onClick = { onDismiss(); onNearbyShare() }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(zh("检查更新")) },
            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            onClick = { onDismiss(); onCheckUpdate() }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(zh("设置")) },
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = { onDismiss(); onOpenSettings() }
        )
    }
}
