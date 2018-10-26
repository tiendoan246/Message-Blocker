package com.tsoft.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Threads;
import android.text.TextUtils;
import android.util.Log;

import com.tsoft.datamodel.ConversationModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.messenger.R;

public class ConversationUtil {

	private Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
	// public static final Uri sAllThreadsUri = Threads.CONTENT_URI.buildUpon()
	// .appendQueryParameter("simple", "true").build();

	private static final String TAG = "ConversationUtil";

	private Context _context;
	private SmsMessageManager smsManager;

	private static final String DATE_COL = "date";
	private static final String MESSAGE_COUNT_COL = "message_count";
	private static final String RECIPIENT_ID_COL = "recipient_ids";
	private static final String SNIPPET_COL = "snippet";
	private static final String SNIPPET_CHARSET_COL = "snippet_cs";
	private static final String READ_COL = "read";
	private static final String ERROR_COL = "error";
	private static final String HAS_ATTACHMENT_COL = "has_attachment";

	public static final String[] ALL_THREADS_PROJECTION = { Threads._ID,
			DATE_COL, MESSAGE_COUNT_COL, RECIPIENT_ID_COL, SNIPPET_COL,
			SNIPPET_CHARSET_COL, READ_COL, ERROR_COL, HAS_ATTACHMENT_COL };

	private static boolean sLoadingThreads;

	private static final int ID = 0;
	private static final int DATE = 1;
	private static final int MESSAGE_COUNT = 2;
	private static final int RECIPIENT_IDS = 3;
	private static final int SNIPPET = 4;
	private static final int SNIPPET_CS = 5;
	private static final int READ = 6;
	private static final int ERROR = 7;
	private static final int HAS_ATTACHMENT = 8;

	private static ConversationUtil _intance;

	private ConversationUtil(Context context) {
		this._context = context;
		this.smsManager = new SmsMessageManager(context);
	}

	public static ConversationUtil getInstance(Context context) {
		if (_intance == null) {
			_intance = new ConversationUtil(context);
		}
		return _intance;
	}

	public List<ConversationModel> getConversations() {
		synchronized (Cache.getInstance()) {
			return Cache.getItems();
		}
	}

	public void cacheAllThreads() {
		synchronized (Cache.getInstance()) {
			if (sLoadingThreads) {
				return;
			}
			sLoadingThreads = true;
		}

		// Where condition
		String where = MESSAGE_COUNT_COL + " > ?";
		String[] args = new String[] { "0" };

		HashSet<Long> threadsOnDisk = new HashSet<Long>();

		// Query for all conversations.
		Cursor c = _context.getContentResolver().query(uri,
				ALL_THREADS_PROJECTION, where, args, DATE_COL + " DESC");
		try {
			if (c != null) {
				while (c.moveToNext()) {
					long threadId = c.getLong(ID);
					threadsOnDisk.add(threadId);

					// Try to find this thread ID in the cache.
					ConversationModel conv;
					synchronized (Cache.getInstance()) {
						conv = Cache.get(threadId);
					}

					if (conv == null) {
						conv = new ConversationModel();
						// Fill the data
						fillFromCursor(conv, c);
						try {
							synchronized (Cache.getInstance()) {
								Cache.put(conv);
							}
						} catch (IllegalStateException e) {
							if (!Cache.replace(conv)) {
							}
						}
					} else {
						fillFromCursor(conv, c);
					}
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
			synchronized (Cache.getInstance()) {
				sLoadingThreads = false;
			}
		}

		// Purge the cache of threads that no longer exist on disk.
		Cache.keepOnly(threadsOnDisk);
	}

	public List<ConversationModel> getAllThreads() {
		List<ConversationModel> conversations = new ArrayList<ConversationModel>();
		Cursor cursor = null;
		try {
			// Where condition
			String where = MESSAGE_COUNT_COL + " > ?";
			String[] args = new String[] { "0" };

			// Query for all conversations.
			cursor = _context.getContentResolver().query(uri,
					ALL_THREADS_PROJECTION, where, args, DATE_COL + " DESC");

			if (cursor != null) {
				while (cursor.moveToNext()) {
					// Try to find this thread ID in the cache.
					ConversationModel conv = new ConversationModel();
					// Fill the data
					fillFromCursor(conv, cursor);
					// Add to the list
					conversations.add(conv);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return conversations;
	}

	public List<ConversationModel> getAllThreads(String search) {
		List<ConversationModel> conversations = new ArrayList<ConversationModel>();
		Cursor cursor = null;
		try {
			// Where condition
			String where = MESSAGE_COUNT_COL + " > ? AND " + SNIPPET_COL + " LIKE ?";
			String[] args = new String[] { "0", "%" + search + "%" };

			// Query for all conversations.
			cursor = _context.getContentResolver().query(uri,
					ALL_THREADS_PROJECTION, where, args, DATE_COL + " DESC");

			if (cursor != null) {
				while (cursor.moveToNext()) {
					// Try to find this thread ID in the cache.
					ConversationModel conv = new ConversationModel();
					// Fill the data
					fillFromCursor(conv, cursor);
					// Add to the list
					conversations.add(conv);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return conversations;
	}
	
	public ConversationModel getConversationById(long id) {
		Cursor cursor = null;
		try {
			// Where condition
			String where = MESSAGE_COUNT_COL + " > ? AND " + Threads._ID
					+ " = ?";
			String[] args = new String[] { "0", String.valueOf(id) };

			// Query for all conversations.
			cursor = _context.getContentResolver().query(uri,
					ALL_THREADS_PROJECTION, where, args, null);

			if (cursor != null) {
				while (cursor.moveToNext()) {
					// Try to find this thread ID in the cache.
					ConversationModel conv = new ConversationModel();
					// Fill the data
					fillFromCursor(conv, cursor);
					// return conversation
					return conv;
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	private void fillFromCursor(ConversationModel conv, Cursor c) {
		synchronized (conv) {
			conv.setId(c.getLong(ID));
			conv.setDate(c.getLong(DATE));
			conv.setMessageCount(c.getInt(MESSAGE_COUNT));

			String snippet = c.getString(SNIPPET);
			if (TextUtils.isEmpty(snippet)) {
				snippet = _context
						.getString(R.string.conversation_snnipet_empty);
			}
			conv.setSnippet(snippet);
			SmsModel firstMessage = smsManager
					.getFirstInboxMessageByThreadID(String.valueOf(conv.getId()));
			if (firstMessage != null) {
				conv.setSender(firstMessage.getPhone());
			} else {
				firstMessage = smsManager.getFirstSentMessageByThreadID(String
						.valueOf(conv.getId()));
				if (firstMessage != null) {
					conv.setSender(firstMessage.getPhone());
				}
			}
		}
	}

	private static class Cache {
		private static Cache sInstance = new Cache();

		static Cache getInstance() {
			return sInstance;
		}

		private final List<ConversationModel> mCache;

		private Cache() {
			mCache = new ArrayList<ConversationModel>(10);
		}

		static List<ConversationModel> getItems() {
			synchronized (sInstance) {
				return sInstance.mCache;
			}
		}

		static ConversationModel get(long threadId) {
			synchronized (sInstance) {
				for (ConversationModel c : sInstance.mCache) {
					if (c.getId() == threadId) {
						return c;
					}
				}
			}
			return null;
		}

		static void put(ConversationModel c) {
			synchronized (sInstance) {
				if (sInstance.mCache.contains(c)) {
					throw new IllegalStateException("cache already contains "
							+ c + " threadId: " + c.getId());
				}
				sInstance.mCache.add(c);
			}
		}

		static boolean replace(ConversationModel c) {
			synchronized (sInstance) {
				if (!sInstance.mCache.contains(c)) {
					return false;
				}

				sInstance.mCache.remove(c);
				sInstance.mCache.add(c);
				return true;
			}
		}

		static void remove(long threadId) {
			synchronized (sInstance) {
				for (ConversationModel c : sInstance.mCache) {
					if (c.getId() == threadId) {
						sInstance.mCache.remove(c);
						return;
					}
				}
			}
		}

		static void keepOnly(Set<Long> threads) {
			synchronized (sInstance) {
				Iterator<ConversationModel> iter = sInstance.mCache.iterator();
				while (iter.hasNext()) {
					ConversationModel c = iter.next();
					if (!threads.contains(c.getId())) {
						iter.remove();
					}
				}
			}
		}
	}

}
