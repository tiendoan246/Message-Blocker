package com.tsoft.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tsoft.datamodel.ContactModel;

public class ContactManagerUtil {

	private static final String TAG = "ContactManagerUtil";
	private Context _context;

	public static final String[] ALL_CONTACTS_PROJECTION = {
			ContactsContract.Contacts._ID,
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
					: ContactsContract.Contacts.DISPLAY_NAME,
			ContactsContract.Contacts.HAS_PHONE_NUMBER,
			ContactsContract.Contacts.PHOTO_THUMBNAIL_URI };

	// The column index for contacts
	private static final int CONTACT_ID_INDEX = 0;
	private static final int CONTACT_DISPLAY_NAME_INDEX = 1;
	private static final int CONTACT_HAS_PHONE_NUMBER_INDEX = 2;
	private static final int CONTACT_PHOTO_THUMBNAIL_URI_INDEX = 3;

	private static ContactManagerUtil _intance;

	private ContactManagerUtil(Context context) {
		this._context = context;
	}

	public static ContactManagerUtil getIntance(Context context) {
		if (_intance == null) {
			_intance = new ContactManagerUtil(context);
		}
		return _intance;
	}

	private static boolean sLoadingContacts;

	public String getMyPhoneNumber() {
		TelephonyManager telemamanger = (TelephonyManager) this._context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telemamanger.getLine1Number();
	}

	public List<ContactModel> getContacts() {
		synchronized (Cache.getInstance()) {
			return Cache.getItems();
		}
	}

	public boolean isContactsEmpty() {
		synchronized (Cache.getInstance()) {
			return Cache.isEmpty();
		}
	}

	public void setContacts(List<ContactModel> contacts) {
		synchronized (Cache.getInstance()) {
			Cache.clearItems();
			for (ContactModel c : contacts) {
				Cache.put(c);
			}
		}
	}

	public ContactModel get(long contactId) {
		synchronized (Cache.getInstance()) {
			return Cache.get(contactId);
		}
	}

	public Bitmap getThumbnailBitmap(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Bitmap bitmap = null;
		Cursor cursor = null;
		try {

			CursorLoader cursorLoader = new CursorLoader(this._context, uri,
					proj, null, null, null);
			cursor = cursorLoader.loadInBackground();

			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

			cursor.moveToFirst();
			long imageId = cursor.getLong(column_index);
			// cursor.close();

			bitmap = MediaStore.Images.Thumbnails.getThumbnail(
					this._context.getContentResolver(), imageId,
					MediaStore.Images.Thumbnails.MINI_KIND,
					(BitmapFactory.Options) null);
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bitmap;
	}

	public void cacheAllThreads() {
		synchronized (Cache.getInstance()) {
			if (sLoadingContacts) {
				return;
			}
			sLoadingContacts = true;
		}

		HashSet<Long> contactsOnDisk = new HashSet<Long>();

		// Query for all conversations.
		Cursor c = _context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, ALL_CONTACTS_PROJECTION,
				null, null, null);
		try {
			if (c != null) {
				while (c.moveToNext()) {
					long contactId = c.getLong(CONTACT_ID_INDEX);
					contactsOnDisk.add(contactId);

					// Try to find this thread ID in the cache.
					ContactModel contact;
					synchronized (Cache.getInstance()) {
						contact = Cache.get(contactId);
					}

					if (contact == null) {
						contact = new ContactModel();
						// Fill the data
						fillFromCursor(contact, c);
						try {
							synchronized (Cache.getInstance()) {
								Cache.put(contact);
							}
						} catch (IllegalStateException e) {
							if (!Cache.replace(contact)) {
							}
						}
					} else {
						fillFromCursor(contact, c);
					}
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
			synchronized (Cache.getInstance()) {
				sLoadingContacts = false;
			}
		}

		// Purge the cache of threads that no longer exist on disk.
		Cache.keepOnly(contactsOnDisk);
	}

	public void cacheAllThreads(Cursor cursor) {
		synchronized (Cache.getInstance()) {
			if (sLoadingContacts) {
				return;
			}
			sLoadingContacts = true;
		}

		HashSet<Long> contactsOnDisk = new HashSet<Long>();
		try {
			if (cursor != null) {
				while (cursor.moveToNext()) {
					long contactId = cursor.getLong(CONTACT_ID_INDEX);
					contactsOnDisk.add(contactId);

					// Try to find this thread ID in the cache.
					ContactModel contact;
					synchronized (Cache.getInstance()) {
						contact = Cache.get(contactId);
					}

					if (contact == null) {
						contact = new ContactModel();
						// Fill the data
						fillFromCursor(contact, cursor);
						try {
							synchronized (Cache.getInstance()) {
								Cache.put(contact);
							}
						} catch (IllegalStateException e) {
							if (!Cache.replace(contact)) {
							}
						}
					} else {
						fillFromCursor(contact, cursor);
					}
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			synchronized (Cache.getInstance()) {
				sLoadingContacts = false;
			}
		}

		// Purge the cache of threads that no longer exist on disk.
		Cache.keepOnly(contactsOnDisk);
	}

	private void fillFromCursor(ContactModel contact, Cursor c) {
		synchronized (contact) {
			contact.setId(c.getLong(CONTACT_ID_INDEX));
			contact.setDisplayName(c.getString(CONTACT_DISPLAY_NAME_INDEX));
			contact.setThumbnailUri(c
					.getString(CONTACT_PHOTO_THUMBNAIL_URI_INDEX));
		}
	}

	private static class Cache {
		private static Cache sInstance = new Cache();

		static Cache getInstance() {
			return sInstance;
		}

		private final List<ContactModel> mCache;

		private Cache() {
			mCache = new ArrayList<ContactModel>();
		}

		static void clearItems() {
			synchronized (sInstance) {
				sInstance.mCache.clear();
			}
		}

		static List<ContactModel> getItems() {
			synchronized (sInstance) {
				return sInstance.mCache;
			}
		}

		static boolean isEmpty() {
			synchronized (sInstance) {
				return sInstance.mCache.isEmpty();
			}
		}

		static ContactModel get(long contactId) {
			synchronized (sInstance) {
				for (ContactModel c : sInstance.mCache) {
					if (c.getId() == contactId) {
						return c;
					}
				}
			}
			return null;
		}

		static void put(ContactModel c) {
			synchronized (sInstance) {
				if (sInstance.mCache.contains(c)) {
					throw new IllegalStateException("cache already contains "
							+ c + " threadId: " + c.getId());
				}
				sInstance.mCache.add(c);
			}
		}

		static boolean replace(ContactModel c) {
			synchronized (sInstance) {
				if (!sInstance.mCache.contains(c)) {
					return false;
				}

				sInstance.mCache.remove(c);
				sInstance.mCache.add(c);
				return true;
			}
		}

		static void remove(long contactId) {
			synchronized (sInstance) {
				for (ContactModel c : sInstance.mCache) {
					if (c.getId() == contactId) {
						sInstance.mCache.remove(c);
						return;
					}
				}
			}
		}

		static void keepOnly(Set<Long> contactIds) {
			synchronized (sInstance) {
				Iterator<ContactModel> iter = sInstance.mCache.iterator();
				while (iter.hasNext()) {
					ContactModel c = iter.next();
					if (!contactIds.contains(c.getId())) {
						iter.remove();
					}
				}
			}
		}
	}
}
