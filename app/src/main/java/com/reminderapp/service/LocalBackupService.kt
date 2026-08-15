package com.reminderapp.service

import com.reminderapp.data.database.AppDatabase
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * v2.1.1: 本地自动备份——自签环境没有 iCloud，把数据写到「下载」目录兜底（用户可见、可导出）。
 * 策略：启动时写一份当日备份；手动可随时备份；保留最近 5 份。
 * Android 10+ 用 MediaStore.Downloads（无需权限）；低版本回落应用专属外部目录。
 */
object LocalBackupService {

    private const val MAX_KEEP = 5
    private const val PREFIX = "reminder_backup_"

    /** 启动时调用：当日同名文件已存在则跳过 */
    fun backupOnLaunch(context: Context) {
        val name = "$PREFIX${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}.json"
        if (hasBackupNamed(context, name)) return
        backupNow(context)
    }

    /** 立即备份，返回文件名（失败返回 null） */
    fun backupNow(context: Context): String? {
        val reminders = AppDatabase.getInstance(context).reminderDao().getAllSyncBlocking()
        val json = BackupService.exportToJson(reminders)
        val name = "$PREFIX${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"

        val ok = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToDownloads(context, name, json)
        } else {
            writeToExternalFiles(context, name, json)
        }
        if (!ok) return null
        trim(context)
        return name
    }

    private fun writeToDownloads(context: Context, name: String, json: String): Boolean {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return false
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            } ?: run {
                context.contentResolver.delete(uri, null, null)
                return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun writeToExternalFiles(context: Context, name: String, json: String): Boolean {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return false
            java.io.File(dir, name).writeText(json)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun hasBackupNamed(context: Context, name: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
                context.contentResolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
                    arrayOf(name),
                    null
                )?.use { return it.count > 0 }
                false
            } else {
                java.io.File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name
                ).exists()
            }
        } catch (e: Exception) {
            false
        }
    }

    /** 只保留最近 MAX_KEEP 份 */
    private fun trim(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME)
                val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                context.contentResolver.query(
                    uri, projection,
                    "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?",
                    arrayOf("$PREFIX%"),
                    "${MediaStore.MediaColumns.DATE_ADDED} DESC"
                )?.use { cursor ->
                    var keep = 0
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(0)
                        keep++
                        if (keep > MAX_KEEP) {
                            context.contentResolver.delete(
                                uri,
                                "${MediaStore.MediaColumns._ID} = ?",
                                arrayOf(id.toString())
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // 清理失败不影响备份本身
        }
    }
}
