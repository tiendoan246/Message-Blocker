package com.tsoft.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Log;

import com.tsoft.datamodel.SmsModel;

public class SmsMessageManager {
	private static final String TAG = "SmsMessageManager";
	// Create Inbox box URI
	private Uri uriInbox = Uri.parse("content://sms/inbox");
	private Context _context;

	public static final String THREAD_ID = "thread_id";
	public static final String ID = "_id";
	public static final String ADDRESS = "address";
	public static final String PERSON = "person";
	// The date the message was received.
	public static final String DATE = "date";
	// The date the message was sent.
	public static final String DATE_SENT = "date_sent";
	public static final String READ = "read";
	public static final String STATUS = "status";
	public static final String TYPE = "type";
	public static final String BODY = "body";
	public static final String SEEN = "seen";

	// Message columns index
	private static final int MESSAGE_ID_INDEX = 0;
	private static final int MESSAGE_ADDRESS_INDEX = 1;
	private static final int MESSAGE_PERSON_INDEX = 2;
	private static final int MESSAGE_BODY_INDEX = 3;
	private static final int MESSAGE_DATE_INDEX = 4;
	private static final int MESSAGE_THREAD_INDEX = 5;
	private static final int MESSAGE_TYPE_INDEX = 6;
	private static final int MESSAGE_STATUS_INDEX = 7;
	private static final int MESSAGE_READ_INDEX = 8;

	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2;

	public static final int MESSAGE_IS_NOT_READ = 0;
	public static final int MESSAGE_IS_READ = 1;

	public static final int MESSAGE_IS_NOT_SEEN = 0;
	public static final int MESSAGE_IS_SEEN = 1;

	/** TP-Status: no status received. */
	public static final int STATUS_NONE = -1;
	/** TP-Status: complete. */
	public static final int STATUS_COMPLETE = 0;
	/** TP-Status: pending. */
	public static final int STATUS_PENDING = 32;
	/** TP-Status: failed. */
	public static final int STATUS_FAILED = 64;

	public static final String MESSAGE_SPAM_PREFIX = "***Spam Message***";

	/*
	 * Column ID - Column Name 0 : _id 1 : thread_id 2 : address 3 : person 4 :
	 * date 5 : protocol 6 : read 7 : status 8 : type 9 : reply_path_present 10
	 * : subject 11 : body 12 : service_center 13 : locked
	 */
	// List required columns
	private String[] cols = new String[] { ID, ADDRESS, PERSON, BODY, DATE,
			THREAD_ID, TYPE, STATUS, READ, SEEN };

	// Get Content Resolver object, which will deal with Content Provider
	private ContentResolver contentResolver;

	public Uri getUriInbox() {
		return uriInbox;
	}

	public void setUriInbox(Uri uriInbox) {
		this.uriInbox = uriInbox;
	}

	public SmsMessageManager(Context context) {
		this._context = context;
	}

	public SmsMessageManager(ContentResolver contentResolver, Context context) {
		this.contentResolver = contentResolver;
		this._context = context;
	}

	public SmsMessageManager(Uri uriInbox, ContentResolver contentResolver,
			Context context) {
		this.uriInbox = uriInbox;
		this.contentResolver = contentResolver;
		this._context = context;
	}

	public void deleteMessage(long id) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		contentResolver.delete(Uri.parse("content://sms/" + id), null, null);
	}

	public void deleteMessageByConversation(String threadId) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = null;
		String[] args = null;

		// Show all message if the option is enabled
		where = THREAD_ID + " = ?";
		args = new String[] { threadId };

		// Delete messages
		contentResolver.delete(Uri.parse("content://sms/"), where, args);
	}

	public void deleteMessages(String[] ids) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}
		// Collection<ContentProviderOperation> operations = new
		// ArrayList<ContentProviderOperation>();
	}

	public ArrayList<SmsModel> getInboxMessages(boolean showAll) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = null;
		String[] args = null;

		// Show all message if the option is enabled
		if (!showAll) {
			where = BODY + " LIKE ?";
			args = new String[] { "%" + MESSAGE_SPAM_PREFIX + "%" };
		}

		// Fetch Inbox SMS Message from Built-in Content Provider
		Cursor c = null;
		ArrayList<SmsModel> messages = new ArrayList<SmsModel>();
		try {
			c = contentResolver.query(this.uriInbox, cols, where, args, DATE
					+ " DESC");
			while (c.moveToNext()) {
				SmsModel sms = new SmsModel();
				sms.setId(c.getLong(MESSAGE_ID_INDEX));
				sms.setPhone(c.getString(MESSAGE_ADDRESS_INDEX));
				sms.setName(c.getString(MESSAGE_PERSON_INDEX));
				sms.setMessage(c.getString(MESSAGE_BODY_INDEX));
				sms.setDate(c.getLong(MESSAGE_DATE_INDEX));
				sms.setStatus(c.getInt(MESSAGE_STATUS_INDEX));
				sms.setRead(c.getInt(MESSAGE_READ_INDEX));
				// Get contact
				// ContactModel contact = ContactManagerUtil.getIntance(
				// this._context).getContactByPhone(sms.getPhone());
				// sms.setContactThumbnailUri(contact != null ? contact
				// .getThumbnailUri() : "");
				sms.setThreadId(c.getLong(MESSAGE_THREAD_INDEX));
				sms.setType(c.getInt(MESSAGE_TYPE_INDEX));
				messages.add(sms);
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return messages;
	}

	public SmsModel getFirstInboxMessageByThreadID(String threadId) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = null;
		String[] args = null;

		// Show all message if the option is enabled
		where = THREAD_ID + " = ?";
		args = new String[] { threadId };

		// Fetch Inbox SMS Message from Built-in Content Provider
		Cursor c = null;
		SmsModel sms = null;
		try {
			c = contentResolver.query(Uri.parse("content://sms/inbox"), cols,
					where, args, null);
			while (c.moveToNext()) {
				sms = new SmsModel();
				sms.setId(c.getLong(MESSAGE_ID_INDEX));
				sms.setPhone(c.getString(MESSAGE_ADDRESS_INDEX));
				sms.setName(c.getString(MESSAGE_PERSON_INDEX));
				sms.setMessage(c.getString(MESSAGE_BODY_INDEX));
				sms.setDate(c.getLong(MESSAGE_DATE_INDEX));
				sms.setStatus(c.getInt(MESSAGE_STATUS_INDEX));
				sms.setRead(c.getInt(MESSAGE_READ_INDEX));
				// Get contact
				// ContactModel contact = ContactManagerUtil.getIntance(
				// this._context).getContactByPhone(sms.getPhone());
				// sms.setContactThumbnailUri(contact != null ? contact
				// .getThumbnailUri() : "");
				sms.setThreadId(c.getLong(MESSAGE_THREAD_INDEX));
				sms.setType(c.getInt(MESSAGE_TYPE_INDEX));
				break;
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return sms;
	}

	public SmsModel getFirstSentMessageByThreadID(String threadId) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = null;
		String[] args = null;

		// Show all message if the option is enabled
		where = THREAD_ID + " = ?";
		args = new String[] { threadId };

		// Fetch Inbox SMS Message from Built-in Content Provider
		Cursor c = null;
		SmsModel sms = null;
		try {
			c = contentResolver.query(Uri.parse("content://sms/sent"), cols,
					where, args, null);
			while (c.moveToNext()) {
				sms = new SmsModel();
				sms.setId(c.getLong(MESSAGE_ID_INDEX));
				sms.setPhone(c.getString(MESSAGE_ADDRESS_INDEX));
				sms.setName(c.getString(MESSAGE_PERSON_INDEX));
				sms.setMessage(c.getString(MESSAGE_BODY_INDEX));
				sms.setDate(c.getLong(MESSAGE_DATE_INDEX));
				sms.setStatus(c.getInt(MESSAGE_STATUS_INDEX));
				sms.setRead(c.getInt(MESSAGE_READ_INDEX));
				// Get contact
				// ContactModel contact = ContactManagerUtil.getIntance(
				// this._context).getContactByPhone(sms.getPhone());
				// sms.setContactThumbnailUri(contact != null ? contact
				// .getThumbnailUri() : "");
				sms.setThreadId(c.getLong(MESSAGE_THREAD_INDEX));
				sms.setType(c.getInt(MESSAGE_TYPE_INDEX));
				break;
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return sms;
	}

	public List<SmsModel> getMessagesByThreadID(String threadId) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = null;
		String[] args = null;

		// Show all message if the option is enabled
		where = THREAD_ID + " = ?";
		args = new String[] { threadId };

		// Fetch SMS Message from Built-in Content Provider
		Cursor c = null;
		List<SmsModel> messages = new ArrayList<SmsModel>();
		try {
			c = contentResolver.query(Uri.parse("content://sms/"), cols, where,
					args, DATE);

			while (c.moveToNext()) {
				SmsModel sms = new SmsModel();
				sms.setId(c.getLong(MESSAGE_ID_INDEX));
				sms.setPhone(c.getString(MESSAGE_ADDRESS_INDEX));
				sms.setName(c.getString(MESSAGE_PERSON_INDEX));
				sms.setMessage(c.getString(MESSAGE_BODY_INDEX));
				sms.setDate(c.getLong(MESSAGE_DATE_INDEX));
				sms.setStatus(c.getInt(MESSAGE_STATUS_INDEX));
				sms.setRead(c.getInt(MESSAGE_READ_INDEX));
				// Get contact
				// ContactModel contact = ContactManagerUtil.getIntance(
				// this._context).getContactByPhone(sms.getPhone());
				// sms.setContactThumbnailUri(contact != null ? contact
				// .getThumbnailUri() : "");
				sms.setThreadId(c.getLong(MESSAGE_THREAD_INDEX));
				sms.setType(c.getInt(MESSAGE_TYPE_INDEX));
				messages.add(sms);
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return messages;
	}

	public int getUnreadMessagesCount() {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = null;
		String[] args = null;

		// Show all message if the option is enabled
		where = READ + " = ?";
		args = new String[] { String.valueOf(MESSAGE_IS_NOT_READ) };

		// Fetch SMS Message from Built-in Content Provider
		Cursor c = null;
		int count = 0;
		try {
			c = contentResolver.query(Uri.parse("content://sms/"), cols, where,
					args, DATE);
			if (c != null) {
				count = c.getCount();
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return count;
	}

	public int getUnreadMessagesCount(String threadId) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = null;
		String[] args = null;

		// Show all message if the option is enabled
		where = THREAD_ID + " = ? AND " + READ + " = ?";
		args = new String[] { threadId, String.valueOf(MESSAGE_IS_NOT_READ) };

		// Fetch SMS Message from Built-in Content Provider
		Cursor c = null;
		int count = 0;
		try {
			c = contentResolver.query(Uri.parse("content://sms/"), cols, where,
					args, DATE);
			if (c != null) {
				count = c.getCount();
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return count;
	}

	public SmsModel getMessagesByID(String id) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = ID + " = ?";
		String[] args = new String[] { id };

		// Fetch SMS Message from Built-in Content Provider
		Cursor c = null;
		SmsModel sms = new SmsModel();
		try {
			c = contentResolver.query(Uri.parse("content://sms/"), cols, where,
					args, null);
			while (c.moveToNext()) {
				sms.setId(c.getLong(MESSAGE_ID_INDEX));
				sms.setPhone(c.getString(MESSAGE_ADDRESS_INDEX));
				sms.setName(c.getString(MESSAGE_PERSON_INDEX));
				sms.setMessage(c.getString(MESSAGE_BODY_INDEX));
				sms.setDate(c.getLong(MESSAGE_DATE_INDEX));
				sms.setStatus(c.getInt(MESSAGE_STATUS_INDEX));
				sms.setRead(c.getInt(MESSAGE_READ_INDEX));
				// Get contact
				// ContactModel contact = ContactManagerUtil.getIntance(
				// this._context).getContactByPhone(sms.getPhone());
				// sms.setContactThumbnailUri(contact != null ? contact
				// .getThumbnailUri() : "");
				sms.setThreadId(c.getLong(MESSAGE_THREAD_INDEX));
				sms.setType(c.getInt(MESSAGE_TYPE_INDEX));
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

	public SmsModel getFirstMessagesByPhone(String phone) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Where condition
		String where = ADDRESS + " = ?";
		String[] args = new String[] { phone };

		// Fetch SMS Message from Built-in Content Provider
		Cursor c = null;
		SmsModel sms = new SmsModel();

		try {
			c = contentResolver.query(Uri.parse("content://sms/"), cols, where,
					args, DATE);
			while (c.moveToNext()) {
				sms.setId(c.getLong(MESSAGE_ID_INDEX));
				sms.setPhone(c.getString(MESSAGE_ADDRESS_INDEX));
				sms.setName(c.getString(MESSAGE_PERSON_INDEX));
				sms.setMessage(c.getString(MESSAGE_BODY_INDEX));
				sms.setDate(c.getLong(MESSAGE_DATE_INDEX));
				sms.setStatus(c.getInt(MESSAGE_STATUS_INDEX));
				sms.setRead(c.getInt(MESSAGE_READ_INDEX));
				// Get contact
				// ContactModel contact = ContactManagerUtil.getIntance(
				// this._context).getContactByPhone(sms.getPhone());
				// sms.setContactThumbnailUri(contact != null ? contact
				// .getThumbnailUri() : "");
				sms.setThreadId(c.getLong(MESSAGE_THREAD_INDEX));
				sms.setType(c.getInt(MESSAGE_TYPE_INDEX));
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

	public void insertSms(ContentResolver contentResolver, SmsMessage sms,
			boolean isSpam) {
		// If this message is spam message, try to modify it
		String message = sms.getMessageBody();
		int readStatus = MESSAGE_IS_NOT_READ;
		if (isSpam) {
			message = MessageFormat.format("{0}{1}", MESSAGE_SPAM_PREFIX,
					sms.getMessageBody());
			readStatus = MESSAGE_IS_READ;
		}

		// Create SMS row
		ContentValues values = new ContentValues();
		values.put(ADDRESS, sms.getOriginatingAddress());
		values.put(DATE, sms.getTimestampMillis());
		values.put(READ, readStatus);
		values.put(STATUS, sms.getStatus());
		values.put(TYPE, MESSAGE_TYPE_INBOX);
		values.put(SEEN, MESSAGE_IS_NOT_SEEN);
		values.put(BODY, message);

		// Push row into the SMS table
		contentResolver.insert(this.uriInbox, values);
	}

	public Uri insertSms(SmsModel sms, Uri _uri) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Create SMS row
		ContentValues values = new ContentValues();
		values.put(ADDRESS, sms.getPhone());
		values.put(DATE, sms.getDate());
		values.put(READ, sms.getRead());
		values.put(STATUS, sms.getStatus());
		values.put(TYPE, sms.getType());
		values.put(SEEN, sms.getSeen());
		values.put(BODY, sms.getMessage());

		// Push row into the SMS table
		return contentResolver.insert(_uri, values);
	}

	public Uri insertSms(SmsModel sms) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Create SMS row
		ContentValues values = new ContentValues();
		values.put(ADDRESS, sms.getPhone());
		values.put(DATE, sms.getDate());
		values.put(READ, sms.getRead());
		values.put(STATUS, sms.getStatus());
		values.put(TYPE, sms.getType());
		values.put(SEEN, sms.getSeen());
		values.put(BODY, sms.getMessage());

		// Push row into the SMS table
		return contentResolver.insert(this.uriInbox, values);
	}

	public void updateSms(SmsModel sms, boolean isSpam) {
		// Don't message that has marked as spam
		if (sms.getMessage().contains(MESSAGE_SPAM_PREFIX)) {
			return;
		}

		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// If this message is spam message, try to modify it
		String message = sms.getMessage();
		int readStatus = MESSAGE_IS_NOT_READ;
		if (isSpam) {
			message = MessageFormat.format("{0}{1}", MESSAGE_SPAM_PREFIX,
					sms.getMessage());
			readStatus = MESSAGE_IS_READ;
		}

		// Create SMS row
		ContentValues values = new ContentValues();
		values.put(ADDRESS, sms.getPhone());
		values.put(READ, readStatus);
		values.put(TYPE, MESSAGE_TYPE_INBOX);
		values.put(SEEN, MESSAGE_IS_NOT_SEEN);
		values.put(BODY, message);

		String where = ID + " = ?";
		String[] args = new String[] { String.valueOf(sms.getId()) };
		contentResolver.update(this.uriInbox, values, where, args);
	}

	public void updateSms(SmsModel sms, Uri _uri) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		// Create SMS row
		ContentValues values = new ContentValues();
		values.put(ADDRESS, sms.getPhone());
		values.put(READ, sms.getRead());
		values.put(TYPE, sms.getType());
		values.put(SEEN, sms.getSeen());
		values.put(BODY, sms.getMessage());
		values.put(STATUS, sms.getStatus());
		values.put(DATE, sms.getDate());

		String where = ID + " = ?";
		String[] args = new String[] { String.valueOf(sms.getId()) };
		contentResolver.update(_uri, values, where, args);
	}

	public void updateSmsReadStatusByThread(String threadId, int readStatus,
			int seen) {
		// Get content resolver
		if (contentResolver == null) {
			contentResolver = this._context.getContentResolver();
		}

		Uri uri = Uri.parse(ConstantUtil.SMS_INBOX_URI);

		// Create SMS row
		ContentValues values = new ContentValues();
		values.put(READ, readStatus);
		values.put(SEEN, seen);

		String where = THREAD_ID + " = ?";
		String[] args = new String[] { threadId };
		contentResolver.update(uri, values, where, args);
	}
}
