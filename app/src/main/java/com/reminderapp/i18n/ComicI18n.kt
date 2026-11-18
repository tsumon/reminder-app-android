package com.reminderapp.i18n

import android.content.Context
import java.util.Locale

/**
 * Fallback multilingual table for Minimalist Cream Comic UI labels not generated in `L.ids`,
 * ensuring zero mixed Chinese/English text when device or app locale is en / zh-TW / ja / ko.
 */
object ComicI18n {
    private data class Entry(val en: String, val tw: String, val ja: String, val ko: String)

    private val table: Map<String, Entry> = mapOf(
        "奶油橙" to Entry("Cream Orange", "奶油橙", "クリームオレンジ", "크림 오렌지"),
        "抹茶奶纸" to Entry("Matcha Paper", "抹茶奶紙", "抹茶ペーパー", "말차 페이퍼"),
        "海盐汽水" to Entry("Sea Salt", "海鹽汽水", "シーソルト", "씨솔트 블루"),
        "莓果奶昔" to Entry("Berry Milk", "莓果奶昔", "ベリーミルク", "베리 밀크"),
        "黄油柠檬" to Entry("Butter Lemon", "奶油檸檬", "バターレモン", "버터 레몬"),
        "复古电紫" to Entry("Retro Lilac", "復古電紫", "レトロライラック", "레트로 라일락"),
        "外观" to Entry("Appearance", "外觀", "外観", "외관"),
        "浅色" to Entry("Light", "淺色", "ライト", "라이트"),
        "深色" to Entry("Dark", "深色", "ダーク", "다크"),
        "新建" to Entry("New", "新增", "新規", "추가"),
        "搜索" to Entry("Search", "搜尋", "検索", "검색"),
        "筛选" to Entry("Filter", "篩選", "絞り込み", "필터"),
        "即将到来" to Entry("Upcoming", "即將到來", "今後の予定", "다가오는 알림"),
        "待确认提醒" to Entry("Awaiting Action", "待確認提醒", "確認待ち", "확인 대기"),
        "今日待办已清空" to Entry("All Caught Up", "今日待辦已清空", "本日の予定は完了", "오늘 할 일 완료"),
        "排期正常" to Entry("On Schedule", "排期正常", "スケジュール正常", "일정 정상"),
        "创建第一个循环提醒，准时记住每件小事" to Entry("Create your first recurring reminder", "建立第一個循環提醒，準時記住每件小事", "最初の定期リマインダーを作成しましょう", "첫 반복 알림을 만들어 보세요"),
        "稍后" to Entry("Snooze", "稍後", "後で", "나중에"),
        "补打今天" to Entry("Make Up", "補打今天", "今日分を記録", "오늘 보충 체크"),
        "高" to Entry("High", "高", "高", "높음"),
        "中" to Entry("Normal", "中", "中", "보통"),
        "低" to Entry("Low", "低", "低", "낮음"),
        "自定义" to Entry("Custom", "自訂", "カスタム", "사용자 지정"),
        "智能建议频率" to Entry("Suggest cycle", "智慧建議頻率", "周期を提案", "주기 추천"),
        "避开节假日/周末" to Entry("Skip holidays & weekends", "避開節假日/週末", "祝日・週末をスキップ", "공휴일/주말 건너뛰기"),
        "落在周末或法定节假日时顺延至下一工作日" to Entry("Defer to next workday when falling on weekends or public holidays", "落在週末或法定節日時順延至下一工作日", "週末や祝日に当たる場合は翌営業日に順延", "주말이나 공휴일이면 다음 평일로 연기"),
        "高级策略与排期预览" to Entry("Advanced policy & schedule preview", "進階策略與排期預覽", "詳細ポリシーと予定プレビュー", "고급 정책 및 일정 미리보기"),
        "报税" to Entry("Tax filing", "報稅", "税務申告", "세금 신고"),
        "域名续期" to Entry("Domain renewal", "網域續期", "ドメイン更新", "도메인 갱신"),
        "生日" to Entry("Birthday", "生日", "誕生日", "생일"),
        "全选" to Entry("Select all", "全選", "すべて選択", "전체 선택"),
        "改时间" to Entry("Reschedule", "改時間", "時刻変更", "시간 변경"),
        "批量管理" to Entry("Batch edit", "批次管理", "一括編集", "일괄 편집"),
        "立即备份" to Entry("Back up now", "立即備份", "今すぐバックアップ", "지금 백업"),
        "备份到下载目录（保留最近 5 份）" to Entry("Save to Downloads (keeps latest 5)", "備份到下載目錄（保留最近 5 份）", "ダウンロードに保存（最新5件保持）", "다운로드 폴더에 백업 (최근 5개 유지)"),
        "恢复本地备份" to Entry("Restore local backup", "還原本地備份", "ローカルバックアップを復元", "로컬 백업 복원"),
        "从下载目录导入已备份的提醒" to Entry("Import backed-up reminders from Downloads", "從下載目錄匯入已備份的提醒", "ダウンロードからバックアップを復元", "다운로드 폴더에서 백업 가져오기"),
        "日历订阅链接" to Entry("Calendar (.ics) feed", "日曆訂閱連結", "カレンダー購読リンク", "캘린더 구독 링크"),
        "上传 .ics 到 WebDAV，供系统日历订阅" to Entry("Upload .ics to WebDAV for calendar subscription", "上傳 .ics 到 WebDAV，供系統日曆訂閱", ".icsをWebDAVへアップロード", "시스템 캘린더 구독용 .ics 업로드"),
        "提醒诊断" to Entry("Diagnostics", "提醒診斷", "リマインダー診断", "알림 진단"),
        "通知权限 / 排期 / 数据规模检查" to Entry("Permissions, schedules & database health", "通知權限 / 排期 / 資料規模檢查", "通知権限・予定・データ診断", "권한 / 일정 / 데이터 점검"),
        "点击自检" to Entry("Run check", "點擊自檢", "診断する", "점검하기"),
        "历史" to Entry("History", "歷史", "履歴", "기록"),
        "清空历史" to Entry("Clear history", "清空歷史", "履歴を消去", "기록 비우기"),
        "思考中…" to Entry("Thinking…", "思考中…", "考え中…", "생각 중…"),
        "公历 %d月%d日" to Entry("Solar %d/%d", "公曆 %d月%d日", "新暦 %d月%d日", "양력 %d/%d")
    )

    fun lookup(context: Context?, src: String): String {
        val entry = table[src] ?: return src
        val locale: Locale = context?.resources?.configuration?.locales?.get(0) ?: Locale.getDefault()
        return when (locale.language) {
            "en" -> entry.en
            "ja" -> entry.ja
            "ko" -> entry.ko
            "zh" -> if (locale.country.equals("TW", true) || locale.country.equals("HK", true) ||
                locale.script.equals("Hant", true)
            ) entry.tw else src
            else -> src
        }
    }
}
