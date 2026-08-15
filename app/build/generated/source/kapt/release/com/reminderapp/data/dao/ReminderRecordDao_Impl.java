package com.reminderapp.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.reminderapp.data.entity.ReminderRecordEntity;
import java.lang.Class;
import java.lang.Exception;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReminderRecordDao_Impl implements ReminderRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReminderRecordEntity> __insertionAdapterOfReminderRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByReminderId;

  public ReminderRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminderRecordEntity = new EntityInsertionAdapter<ReminderRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `reminder_records` (`id`,`reminder_id`,`action`,`timestamp`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReminderRecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getReminderId());
        if (entity.getAction() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAction());
        }
        statement.bindLong(4, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDeleteByReminderId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reminder_records WHERE reminder_id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ReminderRecordEntity record,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReminderRecordEntity.insertAndReturnId(record);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByReminderId(final long reminderId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByReminderId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, reminderId);
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
          __preparedStmtOfDeleteByReminderId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getByReminderId(final long reminderId,
      final Continuation<? super List<ReminderRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM reminder_records WHERE reminder_id = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, reminderId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReminderRecordEntity>>() {
      @Override
      @NonNull
      public List<ReminderRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfReminderId = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_id");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<ReminderRecordEntity> _result = new ArrayList<ReminderRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReminderRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpReminderId;
            _tmpReminderId = _cursor.getLong(_cursorIndexOfReminderId);
            final String _tmpAction;
            if (_cursor.isNull(_cursorIndexOfAction)) {
              _tmpAction = null;
            } else {
              _tmpAction = _cursor.getString(_cursorIndexOfAction);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new ReminderRecordEntity(_tmpId,_tmpReminderId,_tmpAction,_tmpTimestamp);
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
  public Object getAll(final Continuation<? super List<ReminderRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM reminder_records ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReminderRecordEntity>>() {
      @Override
      @NonNull
      public List<ReminderRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfReminderId = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_id");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<ReminderRecordEntity> _result = new ArrayList<ReminderRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReminderRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpReminderId;
            _tmpReminderId = _cursor.getLong(_cursorIndexOfReminderId);
            final String _tmpAction;
            if (_cursor.isNull(_cursorIndexOfAction)) {
              _tmpAction = null;
            } else {
              _tmpAction = _cursor.getString(_cursorIndexOfAction);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new ReminderRecordEntity(_tmpId,_tmpReminderId,_tmpAction,_tmpTimestamp);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
