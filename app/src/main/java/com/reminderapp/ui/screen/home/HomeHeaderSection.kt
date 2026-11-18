package com.reminderapp.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.ui.comic.ComicBrandMark
import com.reminderapp.ui.comic.ComicBrandTitle
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.component.QuickCreate
import com.reminderapp.ui.screen.SmartList
import com.reminderapp.ui.theme.ComicSearchStrip
import com.reminderapp.ui.theme.ThemeStore

@Composable
fun HomeHeaderSection(
    smartList: SmartList,
    onSelectSmartList: (SmartList) -> Unit,
    kindFilter: String,
    onKindFilterChange: (String) -> Unit,
    sortOrder: String,
    onSortOrderChange: (String) -> Unit,
    searchOpen: Boolean,
    onToggleSearch: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectionMode: Boolean,
    selectedCount: Int,
    onToggleSelectAll: () -> Unit,
    onBatchComplete: () -> Unit,
    onBatchDelete: () -> Unit,
    onBatchChangeTime: () -> Unit,
    onExitSelection: () -> Unit,
    onEnterSelection: () -> Unit,
    onCreateReminder: () -> Unit,
    onQuickCreate: (ReminderEntity) -> Unit,
    onAIChat: () -> Unit,
    canUndoCreate: Boolean,
    onUndoCreate: () -> Unit,
    canUndoComplete: Boolean,
    onUndoComplete: () -> Unit,
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
    val colors = comicColors()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var paletteMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .statusBarsPadding()
    ) {
        if (!selectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComicBrandMark(size = 19.dp)
                Spacer(Modifier.width(8.dp))
                ComicBrandTitle(
                    text = zh("循环提醒"),
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                ComicTextAction(
                    text = "AI",
                    onClick = onAIChat,
                    fontSize = 13.5.sp,
                    modifier = Modifier.semantics { contentDescription = zh("AI 助手") }
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (searchOpen || searchQuery.isNotBlank()) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = zh("搜索"),
                    tint = if (searchOpen || searchQuery.isNotBlank()) colors.accent else colors.muted,
                    modifier = Modifier.size(20.dp).clickable(onClick = onToggleSearch)
                )
                Spacer(Modifier.width(8.dp))
                Box {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = zh("更多"),
                        tint = colors.muted,
                        modifier = Modifier.size(20.dp).clickable { menuExpanded = true }
                    )
                    HomeOverflowMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        onEnterSelection = onEnterSelection,
                        onSyncNow = onSyncNow,
                        onOpenSyncSettings = onOpenSyncSettings,
                        onOpenStats = onOpenStats,
                        onImport = onImport,
                        onShowCardImport = onShowCardImport,
                        onExport = onExport,
                        onExportICS = onExportICS,
                        onNearbyShare = onNearbyShare,
                        onCheckUpdate = onCheckUpdate,
                        onOpenSettings = onOpenSettings
                    )
                }
                Spacer(Modifier.width(10.dp))
                ComicButton(
                    text = "+ " + zh("新建"),
                    onClick = onCreateReminder,
                    cut = 5.5.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                    fontSize = 13.sp,
                    modifier = Modifier.semantics { contentDescription = zh("新建提醒") }
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = zhf("已选 %s 项", selectedCount),
                    color = colors.ink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ComicTextAction(text = zh("全选"), onClick = onToggleSelectAll)
                    ComicTextAction(text = zh("完成"), onClick = onBatchComplete, enabled = selectedCount > 0, accent = true)
                    ComicTextAction(text = zh("改时间"), onClick = onBatchChangeTime, enabled = selectedCount > 0)
                    ComicTextAction(text = zh("删除"), onClick = onBatchDelete, enabled = selectedCount > 0, danger = true)
                    ComicTextAction(text = zh("取消"), onClick = onExitSelection)
                }
            }
        }

        // Bare Sub-bar on canvas (renders BOTH "全部" and "今天" on initial launch!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(SmartList.ALL, SmartList.TODAY, SmartList.WEEK).forEach { item ->
                    val active = smartList == item
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSelectSmartList(item) }
                    ) {
                        Text(
                            text = item.label,
                            color = if (active) colors.ink else colors.muted,
                            fontSize = 13.5.sp,
                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
                        )
                        Spacer(Modifier.height(2.dp))
                        Box(
                            Modifier
                                .width(18.dp)
                                .height(2.dp)
                                .background(if (active) colors.accent else colors.canvas)
                        )
                    }
                }
                Box {
                    val filterActive = kindFilter != "all" || sortOrder != "time"
                    Text(
                        text = "▾ " + zh("筛选"),
                        color = if (filterActive) colors.accent else colors.muted,
                        fontSize = 12.5.sp,
                        fontWeight = if (filterActive) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.clickable { filterMenuExpanded = true }
                    )
                    HomeFilterDropdown(
                        expanded = filterMenuExpanded,
                        kindFilter = kindFilter,
                        sortOrder = sortOrder,
                        onKindChange = onKindFilterChange,
                        onSortChange = onSortOrderChange,
                        onDismiss = { filterMenuExpanded = false }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { paletteMenuExpanded = true }.padding(vertical = 2.dp)
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(colors.accent))
                    Spacer(Modifier.width(5.dp))
                    Text(zh(colors.paletteName) + " ▾", color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                HomePaletteDropdown(
                    expanded = paletteMenuExpanded,
                    activeIndex = colors.paletteIndex,
                    onSelect = { ThemeStore.setColorIndex(context, it) },
                    onDismiss = { paletteMenuExpanded = false }
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        ComicHairline(isInner = false)

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            AnimatedVisibility(visible = searchOpen || searchQuery.isNotBlank()) {
                ComicSearchStrip(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            QuickCreate(onEdit = onQuickCreate, onAI = onAIChat)
            if (canUndoCreate || canUndoComplete) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (canUndoCreate) {
                        ComicTextAction("↶ " + zh("撤销刚才的创建"), onClick = onUndoCreate, accent = true, fontSize = 12.5.sp)
                    }
                    if (canUndoComplete) {
                        ComicTextAction("↶ " + zh("撤销刚才的完成"), onClick = onUndoComplete, accent = true, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}
