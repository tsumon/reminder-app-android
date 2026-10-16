package com.reminderapp.service

import android.content.Context
import androidx.room.withTransaction
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.*
import com.reminderapp.receiver.ReminderWidgetProvider

/** All surfaces use this transactional state transition, followed by platform side effects. */
class ReminderActions(private val context: Context, private val db: AppDatabase = AppDatabase.getInstance(context),
    private val notificationAllowed: (ReminderEntity) -> Boolean = { NotificationManager(context).canPostReminder(it) },
    private val holidayRefresh: suspend (Int) -> Unit = { HolidayRemoteService.refresh(context,it) }) {
    enum class Action { COMPLETE, SNOOZE, TOMORROW, SKIP }
    data class Undo(val before: ReminderEntity, val after: ReminderEntity, val recordId: Long, val localVersion: Long)
    data class Result(val reminder: ReminderEntity, val undo: Undo? = null)
    suspend fun apply(expected: ReminderEntity, action: Action, minutes: Long = 15, now: Long = System.currentTimeMillis()): Result? {
        // Complete the derived precheck before committing the action and returning its undo token.
        // Network refresh holds no mutation lock; the final exact comparison rejects any newer edit/action.
        val prepared=if(action in setOf(Action.COMPLETE,Action.SKIP)) {
            var next=if(action==Action.COMPLETE) ReminderEngine.confirm(expected,now) else ReminderEngine.skip(expected,now) ?: return null
            if(expected.holidayAware && next.status!="confirmed")
                next=next.copy(nextTriggerAt=ReminderEngine.deferToWorkday(next.nextTriggerAt,context))
            HolidayPreCheck.adjusted(context,next,now,holidayRefresh)
        } else null
        return ReminderMutationGate.serialized { applyLocked(expected,action,minutes,now,prepared) }
    }
    private suspend fun applyLocked(expected: ReminderEntity, action: Action, minutes: Long, now: Long, prepared: ReminderEntity?): Result? {
        var result: Result? = null
        db.withTransaction {
            val current = db.reminderDao().getById(expected.id) ?: return@withTransaction
            if (!ReminderPolicy.matches(current, expected) || current.status == "confirmed") return@withTransaction
            val next = prepared ?: when (action) {
                Action.COMPLETE -> ReminderEngine.confirm(current, now)
                Action.SNOOZE -> ReminderEngine.snooze(current, minutes, now)
                Action.TOMORROW -> ReminderEngine.snoozeTomorrow(current,now)
                Action.SKIP -> ReminderEngine.skip(current, now) ?: return@withTransaction
            }
            db.reminderDao().update(next)
            val recordId = db.reminderRecordDao().insert(ReminderRecordEntity(reminderId=current.id,
                action=when(action) { Action.COMPLETE -> "confirmed"; Action.SKIP -> "skipped"; else -> "snoozed" }, timestamp=now))
            result = Result(next, if (action == Action.COMPLETE) Undo(current,next,recordId,0) else null)
        }
        val committed = result ?: return null
        val committedVersion=SyncStore.touchLocalChange()
        refresh(committed.reminder)
        return committed.copy(undo=committed.undo?.copy(localVersion=committedVersion))
    }
    suspend fun undo(token: Undo): Boolean = ReminderMutationGate.serialized { undoLocked(token) }
    private suspend fun undoLocked(token: Undo): Boolean {
        var restored: ReminderEntity? = null
        db.withTransaction {
            val current = db.reminderDao().getById(token.after.id) ?: return@withTransaction
            if (!ReminderPolicy.canUndo(current, token.after, SyncStore.localVersion, token.localVersion)) return@withTransaction
            val prior = token.before.copy(revision=current.revision+1)
            db.reminderDao().update(prior)
            db.reminderRecordDao().deleteCompletion(token.recordId)
            restored = prior
        }
        val r = restored ?: return false
        SyncStore.touchLocalChange(); refresh(r); return true
    }
    suspend fun undoCreation(expected: ReminderEntity, expectedVersion: Long): Boolean = ReminderMutationGate.serialized {
        var deleted=false
        db.withTransaction {
            val current=db.reminderDao().getById(expected.id) ?: return@withTransaction
            if(ReminderPolicy.canUndo(current,expected,SyncStore.localVersion,expectedVersion)) {
                db.reminderDao().softDelete(expected.id);deleted=true
            }
        }
        if(deleted) { SyncStore.touchLocalChange();refresh(expected.copy(isActive=false)) }
        deleted
    }

    /** Atomic compare-and-edit, ordered with completion/delivery and their platform effects. */
    suspend fun edit(expected: ReminderEntity, proposed: ReminderEntity): ReminderEntity? = ReminderMutationGate.serialized {
        require(expected.id==proposed.id)
        var committed: ReminderEntity?=null
        db.withTransaction {
            val current=db.reminderDao().getById(expected.id) ?: return@withTransaction
            if(!ReminderPolicy.matches(current,expected)) return@withTransaction
            committed=proposed.copy(id=current.id,syncId=current.syncId,createdAt=current.createdAt,revision=current.revision+1)
            db.reminderDao().update(committed!!)
        }
        committed?.let { r ->
            SyncStore.touchLocalChange()
            QuietHoursStore.set(context,r.id,r.quietStartMinute,r.quietEndMinute)
            refresh(r)
        }
        committed
    }
    data class Created(val reminder: ReminderEntity,val generation: Long)
    suspend fun create(proposed: ReminderEntity): Created = ReminderMutationGate.serialized {
        require(proposed.id==0L)
        val id=db.reminderDao().insert(proposed)
        val saved=proposed.copy(id=id)
        val generation=SyncStore.touchLocalChange()
        QuietHoursStore.set(context,id,saved.quietStartMinute,saved.quietEndMinute)
        refresh(saved)
        Created(saved,generation)
    }

    suspend fun delete(expected: ReminderEntity,removeRecords: Boolean=false): Boolean = ReminderMutationGate.serialized {
        var deleted: ReminderEntity?=null
        db.withTransaction {
            val current=db.reminderDao().getById(expected.id) ?: return@withTransaction
            if(!ReminderPolicy.matches(current,expected)) return@withTransaction
            deleted=current.copy(isActive=false,revision=current.revision+1)
            db.reminderDao().update(deleted!!)
            if(removeRecords) db.reminderRecordDao().deleteByReminderId(current.id)
        }
        deleted?.let { SyncStore.touchLocalChange();refresh(it) }
        deleted!=null
    }
    data class Imported(val saved: List<ReminderEntity>,val skipped: Int)
    suspend fun importItems(items: List<ReminderEntity>): Imported = ReminderMutationGate.serialized {
        val saved=mutableListOf<ReminderEntity>()
        var skipped=0
        db.withTransaction {
            val local=db.reminderDao().getAllIncludingInactive().toMutableList()
            items.forEach { remote ->
                val index=SyncMergePolicy.matchIndex(local,remote)
                val old=local.getOrNull(index)
                if(old!=null) {
                    val normalized=remote.copy(id=old.id,syncId=old.syncId ?: remote.syncId,revision=old.revision)
                    if(remote.syncId.isNullOrBlank() || normalized==old) { skipped++;return@forEach }
                    val next=normalized.copy(revision=old.revision+1)
                    db.reminderDao().update(next);local[index]=next;saved.add(next)
                } else {
                    val fresh=BackupService.ensureSyncId(remote).copy(id=0)
                    val next=fresh.copy(id=db.reminderDao().insert(fresh))
                    local.add(next);saved.add(next)
                }
            }
        }
        if(saved.isNotEmpty()) {
            SyncStore.touchLocalChange()
            saved.forEach { QuietHoursStore.set(context,it.id,it.quietStartMinute,it.quietEndMinute);refresh(it) }
        }
        Imported(saved,skipped)
    }

    private fun refresh(r: ReminderEntity) {
        val scheduler = ReminderScheduler(context)
        scheduler.cancel(r.id)
        NotificationManager(context).cancelReminderNotifications(r.id)
        if (ReminderPolicy.shouldSchedule(r)) scheduler.schedule(r)
        ReminderWidgetProvider.refresh(context)
    }
    /** Claim a delivery atomically; old alarm callbacks never affect a newer occurrence. */
    suspend fun deliver(id: Long, expectedAt: Long, revision: Long, advanceDays: Int = 0, now: Long = System.currentTimeMillis()) = ReminderMutationGate.serialized { deliverLocked(id,expectedAt,revision,advanceDays,now) }
    private suspend fun deliverLocked(id: Long, expectedAt: Long, revision: Long, advanceDays: Int, now: Long) {
        var delivered: ReminderEntity? = null
        var deliveryBefore: ReminderEntity? = null
        var deliveryRecordId: Long? = null
        var deferred: ReminderEntity? = null
        var deferredAdvance: Pair<ReminderEntity,Long>? = null
        db.withTransaction {
            val r = db.reminderDao().getById(id) ?: return@withTransaction
            if (!ReminderPolicy.shouldSchedule(r) || r.nextTriggerAt != expectedAt || r.revision != revision) return@withTransaction
            val quiet = ReminderPolicy.adjustQuiet(r, QuietHoursStore.legacyAdjusted(context,id,now))
            if (advanceDays > 0) {
                if(quiet>now || !notificationAllowed(r)) deferredAdvance=r to maxOf(quiet,if(notificationAllowed(r)) now else now+3_600_000)
                else delivered=r
                return@withTransaction
            }
            if (now < expectedAt) { deferred=r; return@withTransaction }
            if(!notificationAllowed(r)) {
                deferred=r.copy(nextTriggerAt=ReminderPolicy.adjustQuiet(r,now+3_600_000),revision=r.revision+1)
                db.reminderDao().update(deferred!!);return@withTransaction
            }
            if (quiet > now) {
                deferred=r.copy(nextTriggerAt=quiet,revision=r.revision+1)
                db.reminderDao().update(deferred!!); return@withTransaction
            }
            val next = ReminderEngine.escalate(r,now)
            db.reminderDao().update(next)
            deliveryRecordId=db.reminderRecordDao().insert(ReminderRecordEntity(reminderId=id, action="notified",timestamp=now))
            deliveryBefore=r
            delivered=next
        }
        deferredAdvance?.let { (r,at) -> ReminderScheduler(context).scheduleAdvance(r,advanceDays,at) }
        deferred?.let { SyncStore.touchLocalChange();ReminderScheduler(context).schedule(it) }
        val r = delivered ?: return
        val notifications=NotificationManager(context)
        if (advanceDays > 0) {
            try { notifications.sendAdvanceNotification(id,r.title,"$advanceDays 天后 · ${r.note}") }
            catch(_: SecurityException) { ReminderScheduler(context).scheduleAdvance(r,advanceDays,ReminderPolicy.adjustQuiet(r,now+3_600_000)) }
        } else {
            SyncStore.touchLocalChange()
            try {
                if (r.reminderStyle == "important") notifications.sendCriticalReminderNotification(id,r.title,r.note,r)
                else notifications.sendReminderNotification(id,r.title,r.note,r)
            } catch(_: SecurityException) {
                // Permission may be revoked after the capability check. Restore only this delivery claim.
                var retry: ReminderEntity?=null
                db.withTransaction {
                    val latest=db.reminderDao().getById(id)
                    val before=deliveryBefore
                    if(latest==r && before!=null) {
                        retry=before.copy(nextTriggerAt=ReminderPolicy.adjustQuiet(before,now+3_600_000),revision=r.revision+1)
                        db.reminderDao().update(retry!!)
                    }
                    deliveryRecordId?.let { db.reminderRecordDao().deleteNotification(it) }
                }
                SyncStore.touchLocalChange()
                retry?.let { ReminderScheduler(context).schedule(it) }
                return
            }
            // Re-read after notification work: a simultaneous completion must not restore its old retry.
            val latest=db.reminderDao().getById(id)
            if (latest == r && ReminderPolicy.shouldSchedule(r)) ReminderScheduler(context).schedule(r)
            ReminderWidgetProvider.refresh(context)
        }
    }
}
