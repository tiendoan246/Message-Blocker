package com.tsoft.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.datamodel.MessageScheduleModel;
import com.tsoft.datamodel.SmsModel;

public class DatabaseManagerUtil {

	private static final String TAG = "DatabaseManagerUtil";

	private static DatabaseManagerUtil _instance;

	private SqlDatabaseHelper db;

	private DatabaseManagerUtil(Context context) {
		db = new SqlDatabaseHelper(context);
	}

	public static DatabaseManagerUtil getInstance(Context context) {
		if (_instance == null) {
			_instance = new DatabaseManagerUtil(context);
		}
		return _instance;
	}

	public List<SmsModel> getSpamMessages() {
		Cursor c = null;
		List<SmsModel> messages = new ArrayList<SmsModel>();
		try {
			// Fetch Message schedule from database
			c = db.fetchAllSpamMessages();

			do {
				SmsModel sms = new SmsModel();
				sms.setId(c.getLong(SqlDatabaseHelper.KEY_SPAM_ROWID_INDEX));
				sms.setPhone(c.getString(SqlDatabaseHelper.KEY_SPAM_PHONE_INDEX));
				sms.setName(c.getString(SqlDatabaseHelper.KEY_SPAM_NAME_INDEX));
				sms.setMessage(c.getString(SqlDatabaseHelper.KEY_SPAM_MESSAGE_INDEX));
				sms.setDate(c.getLong(SqlDatabaseHelper.KEY_SPAM_DATE_INDEX));
				sms.setSendStatus(c.getString(SqlDatabaseHelper.KEY_SPAM_SENDSTATUS_INDEX));
				sms.setStatus(c.getInt(SqlDatabaseHelper.KEY_SPAM_STATUS_INDEX));
				sms.setRead(c.getInt(SqlDatabaseHelper.KEY_SPAM_READ_INDEX));
				sms.setSeen(c.getInt(SqlDatabaseHelper.KEY_SPAM_SEEN_INDEX));
				messages.add(sms);
			} while (c.moveToNext());
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return messages;
	}
	
	public List<SmsModel> getSpamMessages(String search) {
		Cursor c = null;
		List<SmsModel> messages = new ArrayList<SmsModel>();
		try {
			// Fetch Message schedule from database
			c = db.fetchAllSpamMessages(search);

			do {
				SmsModel sms = new SmsModel();
				sms.setId(c.getLong(SqlDatabaseHelper.KEY_SPAM_ROWID_INDEX));
				sms.setPhone(c.getString(SqlDatabaseHelper.KEY_SPAM_PHONE_INDEX));
				sms.setName(c.getString(SqlDatabaseHelper.KEY_SPAM_NAME_INDEX));
				sms.setMessage(c.getString(SqlDatabaseHelper.KEY_SPAM_MESSAGE_INDEX));
				sms.setDate(c.getLong(SqlDatabaseHelper.KEY_SPAM_DATE_INDEX));
				sms.setSendStatus(c.getString(SqlDatabaseHelper.KEY_SPAM_SENDSTATUS_INDEX));
				sms.setStatus(c.getInt(SqlDatabaseHelper.KEY_SPAM_STATUS_INDEX));
				sms.setRead(c.getInt(SqlDatabaseHelper.KEY_SPAM_READ_INDEX));
				sms.setSeen(c.getInt(SqlDatabaseHelper.KEY_SPAM_SEEN_INDEX));
				messages.add(sms);
			} while (c.moveToNext());
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return messages;
	}
	
	public List<FilterSettingModel> getSettings() {
		Cursor c = null;
		List<FilterSettingModel> settings = new ArrayList<FilterSettingModel>();
		try {
			// Fetch Message schedule from database
			c = db.fetchAllSettings();

			do {
				FilterSettingModel setting = new FilterSettingModel();
				setting.setId(c.getLong(SqlDatabaseHelper.KEY_SETTING_ROWID_INDEX));
				setting.setType(c.getInt(SqlDatabaseHelper.KEY_SETTING_TYPE_INDEX));
				setting.setValue(c.getString(SqlDatabaseHelper.KEY_SETTING_VALUE_INDEX));
				settings.add(setting);
			} while (c.moveToNext());
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return settings;
	}
	
	public List<FilterSettingModel> getSettingsByType(int type) {
		Cursor c = null;
		List<FilterSettingModel> settings = new ArrayList<FilterSettingModel>();
		try {
			// Fetch Message schedule from database
			c = db.fetchAllSettings(type);

			do {
				FilterSettingModel setting = new FilterSettingModel();
				setting.setId(c.getLong(SqlDatabaseHelper.KEY_SETTING_ROWID_INDEX));
				setting.setType(c.getInt(SqlDatabaseHelper.KEY_SETTING_TYPE_INDEX));
				setting.setValue(c.getString(SqlDatabaseHelper.KEY_SETTING_VALUE_INDEX));
				settings.add(setting);
			} while (c.moveToNext());
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return settings;
	}
	
	public List<MessageScheduleModel> getMessageSchedules() {
		Cursor c = null;
		List<MessageScheduleModel> messages = new ArrayList<MessageScheduleModel>();
		try {
			// Fetch Message schedule from database
			c = db.fetchAllMessages();

			do {
				MessageScheduleModel sms = new MessageScheduleModel();
				sms.setId(c.getLong(SqlDatabaseHelper.KEY_ROWID_INDEX));
				sms.setCreatedDate(c
						.getLong(SqlDatabaseHelper.KEY_CREATED_DATE_INDEX));
				sms.setDestinationAddress(c
						.getString(SqlDatabaseHelper.KEY_DESTINATION_ADDRESS_INDEX));
				sms.setMessage(c.getString(SqlDatabaseHelper.KEY_MESSAGE_INDEX));
				sms.setSentDate(c
						.getLong(SqlDatabaseHelper.KEY_SENT_DATE_INDEX));
				sms.setSentOnDate(c
						.getLong(SqlDatabaseHelper.KEY_SENT_ON_DATE_INDEX));
				sms.setSourceAddress(c
						.getString(SqlDatabaseHelper.KEY_SOURCE_ADDRESS_INDEX));
				messages.add(sms);
			} while (c.moveToNext());
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return messages;
	}

	public MessageScheduleModel getFirstMessageScheduleSending() {
		Cursor c = null;
		try {
			// Fetch Message schedule from database
			c = db.fetchFirstMessagesSending();

			if (c != null) {
				MessageScheduleModel sms = new MessageScheduleModel();
				sms.setId(c.getLong(SqlDatabaseHelper.KEY_ROWID_INDEX));
				sms.setCreatedDate(c
						.getLong(SqlDatabaseHelper.KEY_CREATED_DATE_INDEX));
				sms.setDestinationAddress(c
						.getString(SqlDatabaseHelper.KEY_DESTINATION_ADDRESS_INDEX));
				sms.setMessage(c.getString(SqlDatabaseHelper.KEY_MESSAGE_INDEX));
				sms.setSentDate(c
						.getLong(SqlDatabaseHelper.KEY_SENT_DATE_INDEX));
				sms.setSentOnDate(c
						.getLong(SqlDatabaseHelper.KEY_SENT_ON_DATE_INDEX));
				sms.setSourceAddress(c
						.getString(SqlDatabaseHelper.KEY_SOURCE_ADDRESS_INDEX));
				return sms;
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	public SmsModel getSpamMessage(long id) {
		Cursor c = null;
		try {
			// Fetch Message from database
			c = db.fetchSpamMessageById(String.valueOf(id));

			if (c != null) {
				SmsModel sms = new SmsModel();
				sms.setId(c.getLong(SqlDatabaseHelper.KEY_SPAM_ROWID_INDEX));
				sms.setPhone(c.getString(SqlDatabaseHelper.KEY_SPAM_PHONE_INDEX));
				sms.setName(c.getString(SqlDatabaseHelper.KEY_SPAM_NAME_INDEX));
				sms.setMessage(c.getString(SqlDatabaseHelper.KEY_SPAM_MESSAGE_INDEX));
				sms.setDate(c.getLong(SqlDatabaseHelper.KEY_SPAM_DATE_INDEX));
				sms.setSendStatus(c.getString(SqlDatabaseHelper.KEY_SPAM_SENDSTATUS_INDEX));
				sms.setStatus(c.getInt(SqlDatabaseHelper.KEY_SPAM_STATUS_INDEX));
				sms.setRead(c.getInt(SqlDatabaseHelper.KEY_SPAM_READ_INDEX));
				sms.setSeen(c.getInt(SqlDatabaseHelper.KEY_SPAM_SEEN_INDEX));
				return sms;
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}
	
	public FilterSettingModel getSetting(long id) {
		Cursor c = null;
		try {
			// Fetch setting from database
			c = db.fetchSettingById(String.valueOf(id));

			if (c != null) {
				FilterSettingModel setting = new FilterSettingModel();
				setting.setId(c.getLong(SqlDatabaseHelper.KEY_SETTING_ROWID_INDEX));
				setting.setType(c.getInt(SqlDatabaseHelper.KEY_SETTING_TYPE_INDEX));
				setting.setValue(c.getString(SqlDatabaseHelper.KEY_SETTING_VALUE_INDEX));
				return setting;
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}
	
	public MessageScheduleModel getMessageSchedule(long id) {
		Cursor c = null;
		try {
			// Fetch Message schedule from database
			c = db.fetchMessageById(String.valueOf(id));

			if (c != null) {
				MessageScheduleModel sms = new MessageScheduleModel();
				sms.setId(c.getLong(SqlDatabaseHelper.KEY_ROWID_INDEX));
				sms.setCreatedDate(c
						.getLong(SqlDatabaseHelper.KEY_CREATED_DATE_INDEX));
				sms.setDestinationAddress(c
						.getString(SqlDatabaseHelper.KEY_DESTINATION_ADDRESS_INDEX));
				sms.setMessage(c.getString(SqlDatabaseHelper.KEY_MESSAGE_INDEX));
				sms.setSentDate(c
						.getLong(SqlDatabaseHelper.KEY_SENT_DATE_INDEX));
				sms.setSentOnDate(c
						.getLong(SqlDatabaseHelper.KEY_SENT_ON_DATE_INDEX));
				sms.setSourceAddress(c
						.getString(SqlDatabaseHelper.KEY_SOURCE_ADDRESS_INDEX));
				return sms;
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	public long insertSpamMessage(SmsModel message) {
		return db.insertSpamMessage(message);
	}

	public long insertSetting(FilterSettingModel setting) {
		return db.insertSetting(setting);
	}
	
	public long insert(MessageScheduleModel message) {
		return db.insert(message);
	}
	
	public void deleteSpamMessage(long id) {
		db.deleteSpamMessage(id);
	}

	public void deleteSetting(long id) {
		db.deleteSetting(id);
	}
	
	public void deleteSettingByValue(String value) {
		db.deleteSettingByValue(value);
	}
	
	public void delete(long id) {
		db.delete(id);
	}
}
