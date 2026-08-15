package com.reminderapp.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.reminderapp.data.entity.ReminderEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReminderDao_Impl implements ReminderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReminderEntity> __insertionAdapterOfReminderEntity;

  private final EntityDeletionOrUpdateAdapter<ReminderEntity> __updateAdapterOfReminderEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  public ReminderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminderEntity = new EntityInsertionAdapter<ReminderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `reminders` (`id`,`kind`,`cycle`,`custom_days`,`rule_period`,`rule_week`,`rule_weekday`,`date_type`,`target_month`,`target_day`,`holiday_name`,`holiday_id`,`advance_days`,`reminder_hour`,`reminder_minute`,`title`,`note`,`priority`,`status`,`sync_id`,`first_trigger_at`,`next_trigger_at`,`last_confirmed_at`,`retry_count`,`holiday_adjust_note`,`is_critical`,`created_at`,`is_active`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReminderEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getKind() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getKind());
        }
        if (entity.getCycle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCycle());
        }
        statement.bindLong(4, entity.getCustomDays());
        if (entity.getRulePeriod() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRulePeriod());
        }
        if (entity.getRuleWeek() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getRuleWeek());
        }
        if (entity.getRuleWeekday() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getRuleWeekday());
        }
        if (entity.getDateType() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDateType());
        }
        if (entity.getTargetMonth() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getTargetMonth());
        }
        if (entity.getTargetDay() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getTargetDay());
        }
        if (entity.getHolidayName() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getHolidayName());
        }
        if (entity.getHolidayId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getHolidayId());
        }
        statement.bindLong(13, entity.getAdvanceDays());
        statement.bindLong(14, entity.getReminderHour());
        statement.bindLong(15, entity.getReminderMinute());
        if (entity.getTitle() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getTitle());
        }
        if (entity.getNote() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getNote());
        }
        if (entity.getPriority() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getPriority());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getStatus());
        }
        if (entity.getSyncId() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getSyncId());
        }
        statement.bindLong(21, entity.getFirstTriggerAt());
        statement.bindLong(22, entity.getNextTriggerAt());
        if (entity.getLastConfirmedAt() == null) {
          statement.bindNull(23);
        } else {
          statement.bindLong(23, entity.getLastConfirmedAt());
        }
        statement.bindLong(24, entity.getRetryCount());
        if (entity.getHolidayAdjustNote() == null) {
          statement.bindNull(25);
        } else {
          statement.bindString(25, entity.getHolidayAdjustNote());
        }
        final int _tmp = entity.isCritical() ? 1 : 0;
        statement.bindLong(26, _tmp);
        statement.bindLong(27, entity.getCreatedAt());
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(28, _tmp_1);
      }
    };
    this.__updateAdapterOfReminderEntity = new EntityDeletionOrUpdateAdapter<ReminderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reminders` SET `id` = ?,`kind` = ?,`cycle` = ?,`custom_days` = ?,`rule_period` = ?,`rule_week` = ?,`rule_weekday` = ?,`date_type` = ?,`target_month` = ?,`target_day` = ?,`holiday_name` = ?,`holiday_id` = ?,`advance_days` = ?,`reminder_hour` = ?,`reminder_minute` = ?,`title` = ?,`note` = ?,`priority` = ?,`status` = ?,`sync_id` = ?,`first_trigger_at` = ?,`next_trigger_at` = ?,`last_confirmed_at` = ?,`retry_count` = ?,`holiday_adjust_note` = ?,`is_critical` = ?,`created_at` = ?,`is_active` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReminderEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getKind() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getKind());
        }
        if (entity.getCycle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCycle());
        }
        statement.bindLong(4, entity.getCustomDays());
        if (entity.getRulePeriod() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRulePeriod());
        }
        if (entity.getRuleWeek() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getRuleWeek());
        }
        if (entity.getRuleWeekday() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getRuleWeekday());
        }
        if (entity.getDateType() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDateType());
        }
        if (entity.getTargetMonth() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getTargetMonth());
        }
        if (entity.getTargetDay() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getTargetDay());
        }
        if (entity.getHolidayName() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getHolidayName());
        }
        if (entity.getHolidayId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getHolidayId());
        }
        statement.bindLong(13, entity.getAdvanceDays());
        statement.bindLong(14, entity.getReminderHour());
        statement.bindLong(15, entity.getReminderMinute());
        if (entity.getTitle() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getTitle());
        }
        if (entity.getNote() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getNote());
        }
        if (entity.getPriority() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getPriority());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getStatus());
        }
        if (entity.getSyncId() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getSyncId());
        }
        statement.bindLong(21, entity.getFirstTriggerAt());
        statement.bindLong(22, entity.getNextTriggerAt());
        if (entity.getLastConfirmedAt() == null) {
          statement.bindNull(23);
        } else {
          statement.bindLong(23, entity.getLastConfirmedAt());
        }
        statement.bindLong(24, entity.getRetryCount());
        if (entity.getHolidayAdjustNote() == null) {
          statement.bindNull(25);
        } else {
          statement.bindString(25, entity.getHolidayAdjustNote());
        }
        final int _tmp = entity.isCritical() ? 1 : 0;
        statement.bindLong(26, _tmp);
        statement.bindLong(27, entity.getCreatedAt());
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(28, _tmp_1);
        statement.bindLong(29, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reminders WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE reminders SET is_active = 0 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ReminderEntity reminder,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReminderEntity.insertAndReturnId(reminder);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ReminderEntity reminder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReminderEntity.handle(reminder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object softDelete(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSoftDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReminderEntity>> getAllActive() {
    final String _sql = "SELECT * FROM reminders WHERE is_active = 1 ORDER BY CASE priority WHEN 'high' THEN 0 WHEN 'normal' THEN 1 ELSE 2 END, next_trigger_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reminders"}, new Callable<List<ReminderEntity>>() {
      @Override
      @NonNull
      public List<ReminderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
          final int _cursorIndexOfCycle = CursorUtil.getColumnIndexOrThrow(_cursor, "cycle");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_days");
          final int _cursorIndexOfRulePeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_period");
          final int _cursorIndexOfRuleWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_week");
          final int _cursorIndexOfRuleWeekday = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_weekday");
          final int _cursorIndexOfDateType = CursorUtil.getColumnIndexOrThrow(_cursor, "date_type");
          final int _cursorIndexOfTargetMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "target_month");
          final int _cursorIndexOfTargetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "target_day");
          final int _cursorIndexOfHolidayName = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_name");
          final int _cursorIndexOfHolidayId = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_id");
          final int _cursorIndexOfAdvanceDays = CursorUtil.getColumnIndexOrThrow(_cursor, "advance_days");
          final int _cursorIndexOfReminderHour = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_hour");
          final int _cursorIndexOfReminderMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_minute");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_id");
          final int _cursorIndexOfFirstTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "first_trigger_at");
          final int _cursorIndexOfNextTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "next_trigger_at");
          final int _cursorIndexOfLastConfirmedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_confirmed_at");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
          final int _cursorIndexOfHolidayAdjustNote = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_adjust_note");
          final int _cursorIndexOfIsCritical = CursorUtil.getColumnIndexOrThrow(_cursor, "is_critical");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final List<ReminderEntity> _result = new ArrayList<ReminderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReminderEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKind;
            if (_cursor.isNull(_cursorIndexOfKind)) {
              _tmpKind = null;
            } else {
              _tmpKind = _cursor.getString(_cursorIndexOfKind);
            }
            final String _tmpCycle;
            if (_cursor.isNull(_cursorIndexOfCycle)) {
              _tmpCycle = null;
            } else {
              _tmpCycle = _cursor.getString(_cursorIndexOfCycle);
            }
            final int _tmpCustomDays;
            _tmpCustomDays = _cursor.getInt(_cursorIndexOfCustomDays);
            final String _tmpRulePeriod;
            if (_cursor.isNull(_cursorIndexOfRulePeriod)) {
              _tmpRulePeriod = null;
            } else {
              _tmpRulePeriod = _cursor.getString(_cursorIndexOfRulePeriod);
            }
            final Integer _tmpRuleWeek;
            if (_cursor.isNull(_cursorIndexOfRuleWeek)) {
              _tmpRuleWeek = null;
            } else {
              _tmpRuleWeek = _cursor.getInt(_cursorIndexOfRuleWeek);
            }
            final Integer _tmpRuleWeekday;
            if (_cursor.isNull(_cursorIndexOfRuleWeekday)) {
              _tmpRuleWeekday = null;
            } else {
              _tmpRuleWeekday = _cursor.getInt(_cursorIndexOfRuleWeekday);
            }
            final String _tmpDateType;
            if (_cursor.isNull(_cursorIndexOfDateType)) {
              _tmpDateType = null;
            } else {
              _tmpDateType = _cursor.getString(_cursorIndexOfDateType);
            }
            final Integer _tmpTargetMonth;
            if (_cursor.isNull(_cursorIndexOfTargetMonth)) {
              _tmpTargetMonth = null;
            } else {
              _tmpTargetMonth = _cursor.getInt(_cursorIndexOfTargetMonth);
            }
            final Integer _tmpTargetDay;
            if (_cursor.isNull(_cursorIndexOfTargetDay)) {
              _tmpTargetDay = null;
            } else {
              _tmpTargetDay = _cursor.getInt(_cursorIndexOfTargetDay);
            }
            final String _tmpHolidayName;
            if (_cursor.isNull(_cursorIndexOfHolidayName)) {
              _tmpHolidayName = null;
            } else {
              _tmpHolidayName = _cursor.getString(_cursorIndexOfHolidayName);
            }
            final String _tmpHolidayId;
            if (_cursor.isNull(_cursorIndexOfHolidayId)) {
              _tmpHolidayId = null;
            } else {
              _tmpHolidayId = _cursor.getString(_cursorIndexOfHolidayId);
            }
            final int _tmpAdvanceDays;
            _tmpAdvanceDays = _cursor.getInt(_cursorIndexOfAdvanceDays);
            final int _tmpReminderHour;
            _tmpReminderHour = _cursor.getInt(_cursorIndexOfReminderHour);
            final int _tmpReminderMinute;
            _tmpReminderMinute = _cursor.getInt(_cursorIndexOfReminderMinute);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpPriority;
            if (_cursor.isNull(_cursorIndexOfPriority)) {
              _tmpPriority = null;
            } else {
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSyncId;
            if (_cursor.isNull(_cursorIndexOfSyncId)) {
              _tmpSyncId = null;
            } else {
              _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            }
            final long _tmpFirstTriggerAt;
            _tmpFirstTriggerAt = _cursor.getLong(_cursorIndexOfFirstTriggerAt);
            final long _tmpNextTriggerAt;
            _tmpNextTriggerAt = _cursor.getLong(_cursorIndexOfNextTriggerAt);
            final Long _tmpLastConfirmedAt;
            if (_cursor.isNull(_cursorIndexOfLastConfirmedAt)) {
              _tmpLastConfirmedAt = null;
            } else {
              _tmpLastConfirmedAt = _cursor.getLong(_cursorIndexOfLastConfirmedAt);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpHolidayAdjustNote;
            if (_cursor.isNull(_cursorIndexOfHolidayAdjustNote)) {
              _tmpHolidayAdjustNote = null;
            } else {
              _tmpHolidayAdjustNote = _cursor.getString(_cursorIndexOfHolidayAdjustNote);
            }
            final boolean _tmpIsCritical;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCritical);
            _tmpIsCritical = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _item = new ReminderEntity(_tmpId,_tmpKind,_tmpCycle,_tmpCustomDays,_tmpRulePeriod,_tmpRuleWeek,_tmpRuleWeekday,_tmpDateType,_tmpTargetMonth,_tmpTargetDay,_tmpHolidayName,_tmpHolidayId,_tmpAdvanceDays,_tmpReminderHour,_tmpReminderMinute,_tmpTitle,_tmpNote,_tmpPriority,_tmpStatus,_tmpSyncId,_tmpFirstTriggerAt,_tmpNextTriggerAt,_tmpLastConfirmedAt,_tmpRetryCount,_tmpHolidayAdjustNote,_tmpIsCritical,_tmpCreatedAt,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final long id, final Continuation<? super ReminderEntity> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReminderEntity>() {
      @Override
      @Nullable
      public ReminderEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
          final int _cursorIndexOfCycle = CursorUtil.getColumnIndexOrThrow(_cursor, "cycle");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_days");
          final int _cursorIndexOfRulePeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_period");
          final int _cursorIndexOfRuleWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_week");
          final int _cursorIndexOfRuleWeekday = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_weekday");
          final int _cursorIndexOfDateType = CursorUtil.getColumnIndexOrThrow(_cursor, "date_type");
          final int _cursorIndexOfTargetMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "target_month");
          final int _cursorIndexOfTargetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "target_day");
          final int _cursorIndexOfHolidayName = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_name");
          final int _cursorIndexOfHolidayId = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_id");
          final int _cursorIndexOfAdvanceDays = CursorUtil.getColumnIndexOrThrow(_cursor, "advance_days");
          final int _cursorIndexOfReminderHour = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_hour");
          final int _cursorIndexOfReminderMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_minute");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_id");
          final int _cursorIndexOfFirstTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "first_trigger_at");
          final int _cursorIndexOfNextTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "next_trigger_at");
          final int _cursorIndexOfLastConfirmedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_confirmed_at");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
          final int _cursorIndexOfHolidayAdjustNote = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_adjust_note");
          final int _cursorIndexOfIsCritical = CursorUtil.getColumnIndexOrThrow(_cursor, "is_critical");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final ReminderEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKind;
            if (_cursor.isNull(_cursorIndexOfKind)) {
              _tmpKind = null;
            } else {
              _tmpKind = _cursor.getString(_cursorIndexOfKind);
            }
            final String _tmpCycle;
            if (_cursor.isNull(_cursorIndexOfCycle)) {
              _tmpCycle = null;
            } else {
              _tmpCycle = _cursor.getString(_cursorIndexOfCycle);
            }
            final int _tmpCustomDays;
            _tmpCustomDays = _cursor.getInt(_cursorIndexOfCustomDays);
            final String _tmpRulePeriod;
            if (_cursor.isNull(_cursorIndexOfRulePeriod)) {
              _tmpRulePeriod = null;
            } else {
              _tmpRulePeriod = _cursor.getString(_cursorIndexOfRulePeriod);
            }
            final Integer _tmpRuleWeek;
            if (_cursor.isNull(_cursorIndexOfRuleWeek)) {
              _tmpRuleWeek = null;
            } else {
              _tmpRuleWeek = _cursor.getInt(_cursorIndexOfRuleWeek);
            }
            final Integer _tmpRuleWeekday;
            if (_cursor.isNull(_cursorIndexOfRuleWeekday)) {
              _tmpRuleWeekday = null;
            } else {
              _tmpRuleWeekday = _cursor.getInt(_cursorIndexOfRuleWeekday);
            }
            final String _tmpDateType;
            if (_cursor.isNull(_cursorIndexOfDateType)) {
              _tmpDateType = null;
            } else {
              _tmpDateType = _cursor.getString(_cursorIndexOfDateType);
            }
            final Integer _tmpTargetMonth;
            if (_cursor.isNull(_cursorIndexOfTargetMonth)) {
              _tmpTargetMonth = null;
            } else {
              _tmpTargetMonth = _cursor.getInt(_cursorIndexOfTargetMonth);
            }
            final Integer _tmpTargetDay;
            if (_cursor.isNull(_cursorIndexOfTargetDay)) {
              _tmpTargetDay = null;
            } else {
              _tmpTargetDay = _cursor.getInt(_cursorIndexOfTargetDay);
            }
            final String _tmpHolidayName;
            if (_cursor.isNull(_cursorIndexOfHolidayName)) {
              _tmpHolidayName = null;
            } else {
              _tmpHolidayName = _cursor.getString(_cursorIndexOfHolidayName);
            }
            final String _tmpHolidayId;
            if (_cursor.isNull(_cursorIndexOfHolidayId)) {
              _tmpHolidayId = null;
            } else {
              _tmpHolidayId = _cursor.getString(_cursorIndexOfHolidayId);
            }
            final int _tmpAdvanceDays;
            _tmpAdvanceDays = _cursor.getInt(_cursorIndexOfAdvanceDays);
            final int _tmpReminderHour;
            _tmpReminderHour = _cursor.getInt(_cursorIndexOfReminderHour);
            final int _tmpReminderMinute;
            _tmpReminderMinute = _cursor.getInt(_cursorIndexOfReminderMinute);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpPriority;
            if (_cursor.isNull(_cursorIndexOfPriority)) {
              _tmpPriority = null;
            } else {
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSyncId;
            if (_cursor.isNull(_cursorIndexOfSyncId)) {
              _tmpSyncId = null;
            } else {
              _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            }
            final long _tmpFirstTriggerAt;
            _tmpFirstTriggerAt = _cursor.getLong(_cursorIndexOfFirstTriggerAt);
            final long _tmpNextTriggerAt;
            _tmpNextTriggerAt = _cursor.getLong(_cursorIndexOfNextTriggerAt);
            final Long _tmpLastConfirmedAt;
            if (_cursor.isNull(_cursorIndexOfLastConfirmedAt)) {
              _tmpLastConfirmedAt = null;
            } else {
              _tmpLastConfirmedAt = _cursor.getLong(_cursorIndexOfLastConfirmedAt);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpHolidayAdjustNote;
            if (_cursor.isNull(_cursorIndexOfHolidayAdjustNote)) {
              _tmpHolidayAdjustNote = null;
            } else {
              _tmpHolidayAdjustNote = _cursor.getString(_cursorIndexOfHolidayAdjustNote);
            }
            final boolean _tmpIsCritical;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCritical);
            _tmpIsCritical = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _result = new ReminderEntity(_tmpId,_tmpKind,_tmpCycle,_tmpCustomDays,_tmpRulePeriod,_tmpRuleWeek,_tmpRuleWeekday,_tmpDateType,_tmpTargetMonth,_tmpTargetDay,_tmpHolidayName,_tmpHolidayId,_tmpAdvanceDays,_tmpReminderHour,_tmpReminderMinute,_tmpTitle,_tmpNote,_tmpPriority,_tmpStatus,_tmpSyncId,_tmpFirstTriggerAt,_tmpNextTriggerAt,_tmpLastConfirmedAt,_tmpRetryCount,_tmpHolidayAdjustNote,_tmpIsCritical,_tmpCreatedAt,_tmpIsActive);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getBySyncId(final String syncId,
      final Continuation<? super ReminderEntity> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE sync_id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (syncId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, syncId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReminderEntity>() {
      @Override
      @Nullable
      public ReminderEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
          final int _cursorIndexOfCycle = CursorUtil.getColumnIndexOrThrow(_cursor, "cycle");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_days");
          final int _cursorIndexOfRulePeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_period");
          final int _cursorIndexOfRuleWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_week");
          final int _cursorIndexOfRuleWeekday = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_weekday");
          final int _cursorIndexOfDateType = CursorUtil.getColumnIndexOrThrow(_cursor, "date_type");
          final int _cursorIndexOfTargetMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "target_month");
          final int _cursorIndexOfTargetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "target_day");
          final int _cursorIndexOfHolidayName = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_name");
          final int _cursorIndexOfHolidayId = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_id");
          final int _cursorIndexOfAdvanceDays = CursorUtil.getColumnIndexOrThrow(_cursor, "advance_days");
          final int _cursorIndexOfReminderHour = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_hour");
          final int _cursorIndexOfReminderMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_minute");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_id");
          final int _cursorIndexOfFirstTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "first_trigger_at");
          final int _cursorIndexOfNextTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "next_trigger_at");
          final int _cursorIndexOfLastConfirmedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_confirmed_at");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
          final int _cursorIndexOfHolidayAdjustNote = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_adjust_note");
          final int _cursorIndexOfIsCritical = CursorUtil.getColumnIndexOrThrow(_cursor, "is_critical");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final ReminderEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKind;
            if (_cursor.isNull(_cursorIndexOfKind)) {
              _tmpKind = null;
            } else {
              _tmpKind = _cursor.getString(_cursorIndexOfKind);
            }
            final String _tmpCycle;
            if (_cursor.isNull(_cursorIndexOfCycle)) {
              _tmpCycle = null;
            } else {
              _tmpCycle = _cursor.getString(_cursorIndexOfCycle);
            }
            final int _tmpCustomDays;
            _tmpCustomDays = _cursor.getInt(_cursorIndexOfCustomDays);
            final String _tmpRulePeriod;
            if (_cursor.isNull(_cursorIndexOfRulePeriod)) {
              _tmpRulePeriod = null;
            } else {
              _tmpRulePeriod = _cursor.getString(_cursorIndexOfRulePeriod);
            }
            final Integer _tmpRuleWeek;
            if (_cursor.isNull(_cursorIndexOfRuleWeek)) {
              _tmpRuleWeek = null;
            } else {
              _tmpRuleWeek = _cursor.getInt(_cursorIndexOfRuleWeek);
            }
            final Integer _tmpRuleWeekday;
            if (_cursor.isNull(_cursorIndexOfRuleWeekday)) {
              _tmpRuleWeekday = null;
            } else {
              _tmpRuleWeekday = _cursor.getInt(_cursorIndexOfRuleWeekday);
            }
            final String _tmpDateType;
            if (_cursor.isNull(_cursorIndexOfDateType)) {
              _tmpDateType = null;
            } else {
              _tmpDateType = _cursor.getString(_cursorIndexOfDateType);
            }
            final Integer _tmpTargetMonth;
            if (_cursor.isNull(_cursorIndexOfTargetMonth)) {
              _tmpTargetMonth = null;
            } else {
              _tmpTargetMonth = _cursor.getInt(_cursorIndexOfTargetMonth);
            }
            final Integer _tmpTargetDay;
            if (_cursor.isNull(_cursorIndexOfTargetDay)) {
              _tmpTargetDay = null;
            } else {
              _tmpTargetDay = _cursor.getInt(_cursorIndexOfTargetDay);
            }
            final String _tmpHolidayName;
            if (_cursor.isNull(_cursorIndexOfHolidayName)) {
              _tmpHolidayName = null;
            } else {
              _tmpHolidayName = _cursor.getString(_cursorIndexOfHolidayName);
            }
            final String _tmpHolidayId;
            if (_cursor.isNull(_cursorIndexOfHolidayId)) {
              _tmpHolidayId = null;
            } else {
              _tmpHolidayId = _cursor.getString(_cursorIndexOfHolidayId);
            }
            final int _tmpAdvanceDays;
            _tmpAdvanceDays = _cursor.getInt(_cursorIndexOfAdvanceDays);
            final int _tmpReminderHour;
            _tmpReminderHour = _cursor.getInt(_cursorIndexOfReminderHour);
            final int _tmpReminderMinute;
            _tmpReminderMinute = _cursor.getInt(_cursorIndexOfReminderMinute);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpPriority;
            if (_cursor.isNull(_cursorIndexOfPriority)) {
              _tmpPriority = null;
            } else {
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSyncId;
            if (_cursor.isNull(_cursorIndexOfSyncId)) {
              _tmpSyncId = null;
            } else {
              _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            }
            final long _tmpFirstTriggerAt;
            _tmpFirstTriggerAt = _cursor.getLong(_cursorIndexOfFirstTriggerAt);
            final long _tmpNextTriggerAt;
            _tmpNextTriggerAt = _cursor.getLong(_cursorIndexOfNextTriggerAt);
            final Long _tmpLastConfirmedAt;
            if (_cursor.isNull(_cursorIndexOfLastConfirmedAt)) {
              _tmpLastConfirmedAt = null;
            } else {
              _tmpLastConfirmedAt = _cursor.getLong(_cursorIndexOfLastConfirmedAt);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpHolidayAdjustNote;
            if (_cursor.isNull(_cursorIndexOfHolidayAdjustNote)) {
              _tmpHolidayAdjustNote = null;
            } else {
              _tmpHolidayAdjustNote = _cursor.getString(_cursorIndexOfHolidayAdjustNote);
            }
            final boolean _tmpIsCritical;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCritical);
            _tmpIsCritical = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _result = new ReminderEntity(_tmpId,_tmpKind,_tmpCycle,_tmpCustomDays,_tmpRulePeriod,_tmpRuleWeek,_tmpRuleWeekday,_tmpDateType,_tmpTargetMonth,_tmpTargetDay,_tmpHolidayName,_tmpHolidayId,_tmpAdvanceDays,_tmpReminderHour,_tmpReminderMinute,_tmpTitle,_tmpNote,_tmpPriority,_tmpStatus,_tmpSyncId,_tmpFirstTriggerAt,_tmpNextTriggerAt,_tmpLastConfirmedAt,_tmpRetryCount,_tmpHolidayAdjustNote,_tmpIsCritical,_tmpCreatedAt,_tmpIsActive);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDueReminders(final long now,
      final Continuation<? super List<ReminderEntity>> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE is_active = 1 AND next_trigger_at <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReminderEntity>>() {
      @Override
      @NonNull
      public List<ReminderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
          final int _cursorIndexOfCycle = CursorUtil.getColumnIndexOrThrow(_cursor, "cycle");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_days");
          final int _cursorIndexOfRulePeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_period");
          final int _cursorIndexOfRuleWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_week");
          final int _cursorIndexOfRuleWeekday = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_weekday");
          final int _cursorIndexOfDateType = CursorUtil.getColumnIndexOrThrow(_cursor, "date_type");
          final int _cursorIndexOfTargetMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "target_month");
          final int _cursorIndexOfTargetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "target_day");
          final int _cursorIndexOfHolidayName = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_name");
          final int _cursorIndexOfHolidayId = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_id");
          final int _cursorIndexOfAdvanceDays = CursorUtil.getColumnIndexOrThrow(_cursor, "advance_days");
          final int _cursorIndexOfReminderHour = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_hour");
          final int _cursorIndexOfReminderMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_minute");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_id");
          final int _cursorIndexOfFirstTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "first_trigger_at");
          final int _cursorIndexOfNextTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "next_trigger_at");
          final int _cursorIndexOfLastConfirmedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_confirmed_at");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
          final int _cursorIndexOfHolidayAdjustNote = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_adjust_note");
          final int _cursorIndexOfIsCritical = CursorUtil.getColumnIndexOrThrow(_cursor, "is_critical");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final List<ReminderEntity> _result = new ArrayList<ReminderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReminderEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKind;
            if (_cursor.isNull(_cursorIndexOfKind)) {
              _tmpKind = null;
            } else {
              _tmpKind = _cursor.getString(_cursorIndexOfKind);
            }
            final String _tmpCycle;
            if (_cursor.isNull(_cursorIndexOfCycle)) {
              _tmpCycle = null;
            } else {
              _tmpCycle = _cursor.getString(_cursorIndexOfCycle);
            }
            final int _tmpCustomDays;
            _tmpCustomDays = _cursor.getInt(_cursorIndexOfCustomDays);
            final String _tmpRulePeriod;
            if (_cursor.isNull(_cursorIndexOfRulePeriod)) {
              _tmpRulePeriod = null;
            } else {
              _tmpRulePeriod = _cursor.getString(_cursorIndexOfRulePeriod);
            }
            final Integer _tmpRuleWeek;
            if (_cursor.isNull(_cursorIndexOfRuleWeek)) {
              _tmpRuleWeek = null;
            } else {
              _tmpRuleWeek = _cursor.getInt(_cursorIndexOfRuleWeek);
            }
            final Integer _tmpRuleWeekday;
            if (_cursor.isNull(_cursorIndexOfRuleWeekday)) {
              _tmpRuleWeekday = null;
            } else {
              _tmpRuleWeekday = _cursor.getInt(_cursorIndexOfRuleWeekday);
            }
            final String _tmpDateType;
            if (_cursor.isNull(_cursorIndexOfDateType)) {
              _tmpDateType = null;
            } else {
              _tmpDateType = _cursor.getString(_cursorIndexOfDateType);
            }
            final Integer _tmpTargetMonth;
            if (_cursor.isNull(_cursorIndexOfTargetMonth)) {
              _tmpTargetMonth = null;
            } else {
              _tmpTargetMonth = _cursor.getInt(_cursorIndexOfTargetMonth);
            }
            final Integer _tmpTargetDay;
            if (_cursor.isNull(_cursorIndexOfTargetDay)) {
              _tmpTargetDay = null;
            } else {
              _tmpTargetDay = _cursor.getInt(_cursorIndexOfTargetDay);
            }
            final String _tmpHolidayName;
            if (_cursor.isNull(_cursorIndexOfHolidayName)) {
              _tmpHolidayName = null;
            } else {
              _tmpHolidayName = _cursor.getString(_cursorIndexOfHolidayName);
            }
            final String _tmpHolidayId;
            if (_cursor.isNull(_cursorIndexOfHolidayId)) {
              _tmpHolidayId = null;
            } else {
              _tmpHolidayId = _cursor.getString(_cursorIndexOfHolidayId);
            }
            final int _tmpAdvanceDays;
            _tmpAdvanceDays = _cursor.getInt(_cursorIndexOfAdvanceDays);
            final int _tmpReminderHour;
            _tmpReminderHour = _cursor.getInt(_cursorIndexOfReminderHour);
            final int _tmpReminderMinute;
            _tmpReminderMinute = _cursor.getInt(_cursorIndexOfReminderMinute);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpPriority;
            if (_cursor.isNull(_cursorIndexOfPriority)) {
              _tmpPriority = null;
            } else {
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSyncId;
            if (_cursor.isNull(_cursorIndexOfSyncId)) {
              _tmpSyncId = null;
            } else {
              _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            }
            final long _tmpFirstTriggerAt;
            _tmpFirstTriggerAt = _cursor.getLong(_cursorIndexOfFirstTriggerAt);
            final long _tmpNextTriggerAt;
            _tmpNextTriggerAt = _cursor.getLong(_cursorIndexOfNextTriggerAt);
            final Long _tmpLastConfirmedAt;
            if (_cursor.isNull(_cursorIndexOfLastConfirmedAt)) {
              _tmpLastConfirmedAt = null;
            } else {
              _tmpLastConfirmedAt = _cursor.getLong(_cursorIndexOfLastConfirmedAt);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpHolidayAdjustNote;
            if (_cursor.isNull(_cursorIndexOfHolidayAdjustNote)) {
              _tmpHolidayAdjustNote = null;
            } else {
              _tmpHolidayAdjustNote = _cursor.getString(_cursorIndexOfHolidayAdjustNote);
            }
            final boolean _tmpIsCritical;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCritical);
            _tmpIsCritical = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _item = new ReminderEntity(_tmpId,_tmpKind,_tmpCycle,_tmpCustomDays,_tmpRulePeriod,_tmpRuleWeek,_tmpRuleWeekday,_tmpDateType,_tmpTargetMonth,_tmpTargetDay,_tmpHolidayName,_tmpHolidayId,_tmpAdvanceDays,_tmpReminderHour,_tmpReminderMinute,_tmpTitle,_tmpNote,_tmpPriority,_tmpStatus,_tmpSyncId,_tmpFirstTriggerAt,_tmpNextTriggerAt,_tmpLastConfirmedAt,_tmpRetryCount,_tmpHolidayAdjustNote,_tmpIsCritical,_tmpCreatedAt,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllSync(final Continuation<? super List<ReminderEntity>> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE is_active = 1 ORDER BY CASE priority WHEN 'high' THEN 0 WHEN 'normal' THEN 1 ELSE 2 END, next_trigger_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReminderEntity>>() {
      @Override
      @NonNull
      public List<ReminderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
          final int _cursorIndexOfCycle = CursorUtil.getColumnIndexOrThrow(_cursor, "cycle");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_days");
          final int _cursorIndexOfRulePeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_period");
          final int _cursorIndexOfRuleWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_week");
          final int _cursorIndexOfRuleWeekday = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_weekday");
          final int _cursorIndexOfDateType = CursorUtil.getColumnIndexOrThrow(_cursor, "date_type");
          final int _cursorIndexOfTargetMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "target_month");
          final int _cursorIndexOfTargetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "target_day");
          final int _cursorIndexOfHolidayName = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_name");
          final int _cursorIndexOfHolidayId = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_id");
          final int _cursorIndexOfAdvanceDays = CursorUtil.getColumnIndexOrThrow(_cursor, "advance_days");
          final int _cursorIndexOfReminderHour = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_hour");
          final int _cursorIndexOfReminderMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_minute");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_id");
          final int _cursorIndexOfFirstTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "first_trigger_at");
          final int _cursorIndexOfNextTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "next_trigger_at");
          final int _cursorIndexOfLastConfirmedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_confirmed_at");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
          final int _cursorIndexOfHolidayAdjustNote = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_adjust_note");
          final int _cursorIndexOfIsCritical = CursorUtil.getColumnIndexOrThrow(_cursor, "is_critical");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final List<ReminderEntity> _result = new ArrayList<ReminderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReminderEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKind;
            if (_cursor.isNull(_cursorIndexOfKind)) {
              _tmpKind = null;
            } else {
              _tmpKind = _cursor.getString(_cursorIndexOfKind);
            }
            final String _tmpCycle;
            if (_cursor.isNull(_cursorIndexOfCycle)) {
              _tmpCycle = null;
            } else {
              _tmpCycle = _cursor.getString(_cursorIndexOfCycle);
            }
            final int _tmpCustomDays;
            _tmpCustomDays = _cursor.getInt(_cursorIndexOfCustomDays);
            final String _tmpRulePeriod;
            if (_cursor.isNull(_cursorIndexOfRulePeriod)) {
              _tmpRulePeriod = null;
            } else {
              _tmpRulePeriod = _cursor.getString(_cursorIndexOfRulePeriod);
            }
            final Integer _tmpRuleWeek;
            if (_cursor.isNull(_cursorIndexOfRuleWeek)) {
              _tmpRuleWeek = null;
            } else {
              _tmpRuleWeek = _cursor.getInt(_cursorIndexOfRuleWeek);
            }
            final Integer _tmpRuleWeekday;
            if (_cursor.isNull(_cursorIndexOfRuleWeekday)) {
              _tmpRuleWeekday = null;
            } else {
              _tmpRuleWeekday = _cursor.getInt(_cursorIndexOfRuleWeekday);
            }
            final String _tmpDateType;
            if (_cursor.isNull(_cursorIndexOfDateType)) {
              _tmpDateType = null;
            } else {
              _tmpDateType = _cursor.getString(_cursorIndexOfDateType);
            }
            final Integer _tmpTargetMonth;
            if (_cursor.isNull(_cursorIndexOfTargetMonth)) {
              _tmpTargetMonth = null;
            } else {
              _tmpTargetMonth = _cursor.getInt(_cursorIndexOfTargetMonth);
            }
            final Integer _tmpTargetDay;
            if (_cursor.isNull(_cursorIndexOfTargetDay)) {
              _tmpTargetDay = null;
            } else {
              _tmpTargetDay = _cursor.getInt(_cursorIndexOfTargetDay);
            }
            final String _tmpHolidayName;
            if (_cursor.isNull(_cursorIndexOfHolidayName)) {
              _tmpHolidayName = null;
            } else {
              _tmpHolidayName = _cursor.getString(_cursorIndexOfHolidayName);
            }
            final String _tmpHolidayId;
            if (_cursor.isNull(_cursorIndexOfHolidayId)) {
              _tmpHolidayId = null;
            } else {
              _tmpHolidayId = _cursor.getString(_cursorIndexOfHolidayId);
            }
            final int _tmpAdvanceDays;
            _tmpAdvanceDays = _cursor.getInt(_cursorIndexOfAdvanceDays);
            final int _tmpReminderHour;
            _tmpReminderHour = _cursor.getInt(_cursorIndexOfReminderHour);
            final int _tmpReminderMinute;
            _tmpReminderMinute = _cursor.getInt(_cursorIndexOfReminderMinute);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpPriority;
            if (_cursor.isNull(_cursorIndexOfPriority)) {
              _tmpPriority = null;
            } else {
              _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSyncId;
            if (_cursor.isNull(_cursorIndexOfSyncId)) {
              _tmpSyncId = null;
            } else {
              _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            }
            final long _tmpFirstTriggerAt;
            _tmpFirstTriggerAt = _cursor.getLong(_cursorIndexOfFirstTriggerAt);
            final long _tmpNextTriggerAt;
            _tmpNextTriggerAt = _cursor.getLong(_cursorIndexOfNextTriggerAt);
            final Long _tmpLastConfirmedAt;
            if (_cursor.isNull(_cursorIndexOfLastConfirmedAt)) {
              _tmpLastConfirmedAt = null;
            } else {
              _tmpLastConfirmedAt = _cursor.getLong(_cursorIndexOfLastConfirmedAt);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpHolidayAdjustNote;
            if (_cursor.isNull(_cursorIndexOfHolidayAdjustNote)) {
              _tmpHolidayAdjustNote = null;
            } else {
              _tmpHolidayAdjustNote = _cursor.getString(_cursorIndexOfHolidayAdjustNote);
            }
            final boolean _tmpIsCritical;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCritical);
            _tmpIsCritical = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _item = new ReminderEntity(_tmpId,_tmpKind,_tmpCycle,_tmpCustomDays,_tmpRulePeriod,_tmpRuleWeek,_tmpRuleWeekday,_tmpDateType,_tmpTargetMonth,_tmpTargetDay,_tmpHolidayName,_tmpHolidayId,_tmpAdvanceDays,_tmpReminderHour,_tmpReminderMinute,_tmpTitle,_tmpNote,_tmpPriority,_tmpStatus,_tmpSyncId,_tmpFirstTriggerAt,_tmpNextTriggerAt,_tmpLastConfirmedAt,_tmpRetryCount,_tmpHolidayAdjustNote,_tmpIsCritical,_tmpCreatedAt,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public List<ReminderEntity> getAllSyncBlocking() {
    final String _sql = "SELECT * FROM reminders WHERE is_active = 1 ORDER BY CASE priority WHEN 'high' THEN 0 WHEN 'normal' THEN 1 ELSE 2 END, next_trigger_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
      final int _cursorIndexOfCycle = CursorUtil.getColumnIndexOrThrow(_cursor, "cycle");
      final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_days");
      final int _cursorIndexOfRulePeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_period");
      final int _cursorIndexOfRuleWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_week");
      final int _cursorIndexOfRuleWeekday = CursorUtil.getColumnIndexOrThrow(_cursor, "rule_weekday");
      final int _cursorIndexOfDateType = CursorUtil.getColumnIndexOrThrow(_cursor, "date_type");
      final int _cursorIndexOfTargetMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "target_month");
      final int _cursorIndexOfTargetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "target_day");
      final int _cursorIndexOfHolidayName = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_name");
      final int _cursorIndexOfHolidayId = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_id");
      final int _cursorIndexOfAdvanceDays = CursorUtil.getColumnIndexOrThrow(_cursor, "advance_days");
      final int _cursorIndexOfReminderHour = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_hour");
      final int _cursorIndexOfReminderMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_minute");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_id");
      final int _cursorIndexOfFirstTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "first_trigger_at");
      final int _cursorIndexOfNextTriggerAt = CursorUtil.getColumnIndexOrThrow(_cursor, "next_trigger_at");
      final int _cursorIndexOfLastConfirmedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_confirmed_at");
      final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
      final int _cursorIndexOfHolidayAdjustNote = CursorUtil.getColumnIndexOrThrow(_cursor, "holiday_adjust_note");
      final int _cursorIndexOfIsCritical = CursorUtil.getColumnIndexOrThrow(_cursor, "is_critical");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
      final List<ReminderEntity> _result = new ArrayList<ReminderEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ReminderEntity _item;
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        final String _tmpKind;
        if (_cursor.isNull(_cursorIndexOfKind)) {
          _tmpKind = null;
        } else {
          _tmpKind = _cursor.getString(_cursorIndexOfKind);
        }
        final String _tmpCycle;
        if (_cursor.isNull(_cursorIndexOfCycle)) {
          _tmpCycle = null;
        } else {
          _tmpCycle = _cursor.getString(_cursorIndexOfCycle);
        }
        final int _tmpCustomDays;
        _tmpCustomDays = _cursor.getInt(_cursorIndexOfCustomDays);
        final String _tmpRulePeriod;
        if (_cursor.isNull(_cursorIndexOfRulePeriod)) {
          _tmpRulePeriod = null;
        } else {
          _tmpRulePeriod = _cursor.getString(_cursorIndexOfRulePeriod);
        }
        final Integer _tmpRuleWeek;
        if (_cursor.isNull(_cursorIndexOfRuleWeek)) {
          _tmpRuleWeek = null;
        } else {
          _tmpRuleWeek = _cursor.getInt(_cursorIndexOfRuleWeek);
        }
        final Integer _tmpRuleWeekday;
        if (_cursor.isNull(_cursorIndexOfRuleWeekday)) {
          _tmpRuleWeekday = null;
        } else {
          _tmpRuleWeekday = _cursor.getInt(_cursorIndexOfRuleWeekday);
        }
        final String _tmpDateType;
        if (_cursor.isNull(_cursorIndexOfDateType)) {
          _tmpDateType = null;
        } else {
          _tmpDateType = _cursor.getString(_cursorIndexOfDateType);
        }
        final Integer _tmpTargetMonth;
        if (_cursor.isNull(_cursorIndexOfTargetMonth)) {
          _tmpTargetMonth = null;
        } else {
          _tmpTargetMonth = _cursor.getInt(_cursorIndexOfTargetMonth);
        }
        final Integer _tmpTargetDay;
        if (_cursor.isNull(_cursorIndexOfTargetDay)) {
          _tmpTargetDay = null;
        } else {
          _tmpTargetDay = _cursor.getInt(_cursorIndexOfTargetDay);
        }
        final String _tmpHolidayName;
        if (_cursor.isNull(_cursorIndexOfHolidayName)) {
          _tmpHolidayName = null;
        } else {
          _tmpHolidayName = _cursor.getString(_cursorIndexOfHolidayName);
        }
        final String _tmpHolidayId;
        if (_cursor.isNull(_cursorIndexOfHolidayId)) {
          _tmpHolidayId = null;
        } else {
          _tmpHolidayId = _cursor.getString(_cursorIndexOfHolidayId);
        }
        final int _tmpAdvanceDays;
        _tmpAdvanceDays = _cursor.getInt(_cursorIndexOfAdvanceDays);
        final int _tmpReminderHour;
        _tmpReminderHour = _cursor.getInt(_cursorIndexOfReminderHour);
        final int _tmpReminderMinute;
        _tmpReminderMinute = _cursor.getInt(_cursorIndexOfReminderMinute);
        final String _tmpTitle;
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _tmpTitle = null;
        } else {
          _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        }
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        final String _tmpPriority;
        if (_cursor.isNull(_cursorIndexOfPriority)) {
          _tmpPriority = null;
        } else {
          _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
        }
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        final String _tmpSyncId;
        if (_cursor.isNull(_cursorIndexOfSyncId)) {
          _tmpSyncId = null;
        } else {
          _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
        }
        final long _tmpFirstTriggerAt;
        _tmpFirstTriggerAt = _cursor.getLong(_cursorIndexOfFirstTriggerAt);
        final long _tmpNextTriggerAt;
        _tmpNextTriggerAt = _cursor.getLong(_cursorIndexOfNextTriggerAt);
        final Long _tmpLastConfirmedAt;
        if (_cursor.isNull(_cursorIndexOfLastConfirmedAt)) {
          _tmpLastConfirmedAt = null;
        } else {
          _tmpLastConfirmedAt = _cursor.getLong(_cursorIndexOfLastConfirmedAt);
        }
        final int _tmpRetryCount;
        _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
        final String _tmpHolidayAdjustNote;
        if (_cursor.isNull(_cursorIndexOfHolidayAdjustNote)) {
          _tmpHolidayAdjustNote = null;
        } else {
          _tmpHolidayAdjustNote = _cursor.getString(_cursorIndexOfHolidayAdjustNote);
        }
        final boolean _tmpIsCritical;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsCritical);
        _tmpIsCritical = _tmp != 0;
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        final boolean _tmpIsActive;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp_1 != 0;
        _item = new ReminderEntity(_tmpId,_tmpKind,_tmpCycle,_tmpCustomDays,_tmpRulePeriod,_tmpRuleWeek,_tmpRuleWeekday,_tmpDateType,_tmpTargetMonth,_tmpTargetDay,_tmpHolidayName,_tmpHolidayId,_tmpAdvanceDays,_tmpReminderHour,_tmpReminderMinute,_tmpTitle,_tmpNote,_tmpPriority,_tmpStatus,_tmpSyncId,_tmpFirstTriggerAt,_tmpNextTriggerAt,_tmpLastConfirmedAt,_tmpRetryCount,_tmpHolidayAdjustNote,_tmpIsCritical,_tmpCreatedAt,_tmpIsActive);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Object delete(final ReminderEntity reminder,
      final Continuation<? super Unit> $completion) {
    return ReminderDao.DefaultImpls.delete(ReminderDao_Impl.this, reminder, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
