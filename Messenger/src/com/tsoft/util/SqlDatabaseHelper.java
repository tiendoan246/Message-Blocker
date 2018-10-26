package com.tsoft.util;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.datamodel.MessageScheduleModel;
import com.tsoft.datamodel.SmsModel;

public class SqlDatabaseHelper extends SQLiteOpenHelper {

	// Messenger rows, schedule message
	public static final String KEY_ROWID = "_id";
	public static final String KEY_SOURCE_ADDRESS = "s_address";
	public static final String KEY_DESTINATION_ADDRESS = "d_addresses";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_CREATED_DATE = "created_date";
	public static final String KEY_SENT_DATE = "sent_date";
	public static final String KEY_SENT_ON_DATE = "sent_ondate";

	// Messenger index rows, schedule message
	public static final int KEY_ROWID_INDEX = 0;
	public static final int KEY_SOURCE_ADDRESS_INDEX = 1;
	public static final int KEY_DESTINATION_ADDRESS_INDEX = 2;
	public static final int KEY_MESSAGE_INDEX = 3;
	public static final int KEY_CREATED_DATE_INDEX = 4;
	public static final int KEY_SENT_DATE_INDEX = 5;
	public static final int KEY_SENT_ON_DATE_INDEX = 6;

	// Message spam rows
	public static final String KEY_SPAM_ROWID = "_id";
	public static final String KEY_SPAM_PHONE = "_phone";
	public static final String KEY_SPAM_NAME = "_name";
	public static final String KEY_SPAM_MESSAGE = "_message";
	public static final String KEY_SPAM_DATE = "_date";
	public static final String KEY_SPAM_SENDSTATUS = "_sendstatus";
	public static final String KEY_SPAM_STATUS = "_status";
	public static final String KEY_SPAM_READ = "_read";
	public static final String KEY_SPAM_SEEN = "_seen";

	// Message spam index rows
	public static final int KEY_SPAM_ROWID_INDEX = 0;
	public static final int KEY_SPAM_PHONE_INDEX = 1;
	public static final int KEY_SPAM_NAME_INDEX = 2;
	public static final int KEY_SPAM_MESSAGE_INDEX = 3;
	public static final int KEY_SPAM_DATE_INDEX = 4;
	public static final int KEY_SPAM_SENDSTATUS_INDEX = 5;
	public static final int KEY_SPAM_STATUS_INDEX = 6;
	public static final int KEY_SPAM_READ_INDEX = 7;
	public static final int KEY_SPAM_SEEN_INDEX = 8;

	// Filter setting rows
	public static final String KEY_SETTING_ROWID = "_id";
	public static final String KEY_SETTING_TYPE = "_type";
	public static final String KEY_SETTING_VALUE = "_value";

	// Filter setting index rows
	public static final int KEY_SETTING_ROWID_INDEX = 0;
	public static final int KEY_SETTING_TYPE_INDEX = 1;
	public static final int KEY_SETTING_VALUE_INDEX = 2;

	public static final String DATABASE_NAME = "tsoftmessenger";
	public static final int DATABASE_VERSION = 1;

	public static final String DATABASE_TABLE = "messenger";
	public static final String DATABASE_TABLE_SPAM = "spam";
	public static final String DATABASE_TABLE_FILTER = "filter";

	// Create table query
	private static String CREATE_MESSENGER_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE
			+ "("
			+ KEY_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ KEY_SOURCE_ADDRESS
			+ " TEXT, "
			+ KEY_DESTINATION_ADDRESS
			+ " TEXT, "
			+ KEY_MESSAGE
			+ " TEXT, "
			+ KEY_CREATED_DATE
			+ " BIGINT, "
			+ KEY_SENT_DATE
			+ " BIGINT, " + KEY_SENT_ON_DATE + " BIGINT  )";

	private static String CREATE_SPAM_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_SPAM + "(" + KEY_SPAM_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_SPAM_PHONE
			+ " TEXT, " + KEY_SPAM_NAME + " TEXT, " + KEY_SPAM_MESSAGE
			+ " TEXT, " + KEY_SPAM_DATE + " BIGINT, " + KEY_SPAM_SENDSTATUS
			+ " INT, " + KEY_SPAM_STATUS + " INT, " + KEY_SPAM_READ + " INT, "
			+ KEY_SPAM_SEEN + " INT )";

	private static String CREATE_SETTING_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_FILTER + "(" + KEY_SETTING_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_SETTING_TYPE
			+ " INT, " + KEY_SETTING_VALUE + " TEXT )";

	private String[] messageCols = new String[] { KEY_ROWID,
			KEY_SOURCE_ADDRESS, KEY_DESTINATION_ADDRESS, KEY_MESSAGE,
			KEY_CREATED_DATE, KEY_SENT_DATE, KEY_SENT_ON_DATE };

	private String[] spamMessageCols = new String[] { KEY_SPAM_ROWID,
			KEY_SPAM_PHONE, KEY_SPAM_NAME, KEY_SPAM_MESSAGE, KEY_SPAM_DATE,
			KEY_SPAM_SENDSTATUS, KEY_SPAM_STATUS, KEY_SPAM_READ, KEY_SPAM_SEEN };

	private String[] settingCols = new String[] { KEY_SETTING_ROWID,
			KEY_SETTING_TYPE, KEY_SETTING_VALUE };

	public SqlDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_MESSENGER_TABLE);
		db.execSQL(CREATE_SPAM_TABLE);
		db.execSQL(CREATE_SETTING_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXITS " + DATABASE_TABLE);
		db.execSQL("DROP TABLE IF EXITS " + DATABASE_TABLE_SPAM);
		db.execSQL("DROP TABLE IF EXITS " + DATABASE_TABLE_FILTER);
		onCreate(db);

	}

	public Cursor fetchAllSettings() {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor mCursor = db.query(DATABASE_TABLE_FILTER, settingCols, null,
				null, null, null, KEY_SETTING_VALUE);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllSettings(int type) {
		SQLiteDatabase db = this.getReadableDatabase();

		String whereClause = KEY_SETTING_TYPE + " = ?";
		String[] whereArgs = new String[] { String.valueOf(type) };

		Cursor mCursor = db.query(DATABASE_TABLE_FILTER, settingCols,
				whereClause, whereArgs, null, null, KEY_SETTING_VALUE);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllSpamMessages() {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor mCursor = db.query(DATABASE_TABLE_SPAM, spamMessageCols, null,
				null, null, null, KEY_SPAM_DATE);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllSpamMessages(String search) {
		SQLiteDatabase db = this.getReadableDatabase();

		// Where condition
		String where = KEY_SPAM_MESSAGE + " LIKE ?";
		String[] args = new String[] { "%" + search + "%" };

		Cursor mCursor = db.query(DATABASE_TABLE_SPAM, spamMessageCols, where,
				args, null, null, KEY_SPAM_DATE);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllMessages() {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor mCursor = db.query(DATABASE_TABLE, messageCols, null, null,
				null, null, KEY_SENT_ON_DATE);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchFirstMessagesSending() {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor mCursor = db.query(DATABASE_TABLE, messageCols, null, null,
				null, null, KEY_SENT_ON_DATE + " LIMIT 1");
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchMessageByPhone(String phone) throws SQLException {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor mCursor = null;
		if (phone == null || phone.length() == 0) {
			mCursor = db.query(DATABASE_TABLE, messageCols, null, null, null,
					null, null);
		} else {
			mCursor = db.rawQuery("SELECT * FROM " + DATABASE_TABLE + " WHERE "
					+ KEY_DESTINATION_ADDRESS + " = ?", new String[] { phone });
		}

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchSettingById(String id) throws SQLException {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor mCursor = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_FILTER
				+ " WHERE " + KEY_SETTING_ROWID + " = ?", new String[] { id });

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchSpamMessageById(String id) throws SQLException {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor mCursor = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_SPAM
				+ " WHERE " + KEY_SPAM_ROWID + " = ?", new String[] { id });

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchMessageById(String id) throws SQLException {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor mCursor = db.rawQuery("SELECT * FROM " + DATABASE_TABLE
				+ " WHERE " + KEY_ROWID + " = ?", new String[] { id });

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public long insertSetting(FilterSettingModel setting) {
		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SETTING_TYPE, setting.getType());
		values.put(KEY_SETTING_VALUE, setting.getValue());

		// insert the row
		return db.insert(DATABASE_TABLE_FILTER, null, values);
	}

	public long insertSpamMessage(SmsModel message) {
		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SPAM_PHONE, message.getPhone());
		values.put(KEY_SPAM_NAME, message.getName());
		values.put(KEY_SPAM_MESSAGE, message.getMessage());
		values.put(KEY_SPAM_DATE, message.getDate());
		values.put(KEY_SPAM_SENDSTATUS, message.getSendStatus());
		values.put(KEY_SPAM_STATUS, message.getStatus());
		values.put(KEY_SPAM_READ, message.getRead());
		values.put(KEY_SPAM_SEEN, message.getSeen());

		// insert the row
		return db.insert(DATABASE_TABLE_SPAM, null, values);
	}

	public long insert(MessageScheduleModel message) {
		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SOURCE_ADDRESS, message.getSourceAddress());
		values.put(KEY_DESTINATION_ADDRESS, message.getDestinationAddress());
		values.put(KEY_MESSAGE, message.getMessage());
		values.put(KEY_CREATED_DATE, new Date().getTime());
		values.put(KEY_SENT_DATE, message.getSentDate());
		values.put(KEY_SENT_ON_DATE, message.getSentOnDate());

		// insert the row
		return db.insert(DATABASE_TABLE, null, values);
	}

	public void deleteSetting(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		db.delete(DATABASE_TABLE_FILTER, KEY_SETTING_ROWID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}

	public void deleteSettingByValue(String value) {
		SQLiteDatabase db = this.getReadableDatabase();

		db.delete(DATABASE_TABLE_FILTER, KEY_SETTING_VALUE + " = ?",
				new String[] { value });
		db.close();
	}

	public void deleteSpamMessage(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		db.delete(DATABASE_TABLE_SPAM, KEY_SPAM_ROWID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}

	public void delete(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		db.delete(DATABASE_TABLE, KEY_ROWID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}
}
