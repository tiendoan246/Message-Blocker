package com.tsoft.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.tsoft.datamodel.PhoneModel;

public class PhoneManagerUtil {

	private Context _context;

	public static final String[] PHONE_PROJECTION = {
			ContactsContract.CommonDataKinds.Phone._ID,
			ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Phone.NUMBER };

	// The column index for contacts
	private static final int PHONE_ID_INDEX = 0;
	private static final int PHONE_CONTACT_ID_INDEX = 1;
	private static final int PHONE_DISPLAY_NAME_INDEX = 2;
	private static final int PHONE_NUMBER_INDEX = 3;

	// Phone region
	private static final String PHONE_VN = "+84";
	private static final String PHONE_NUM_VALUE = "0";

	// Phone region map
	private HashMap<String, String> phoneRegions;
	private HashMap<String, String> phoneRegionValues;

	private String countryCode;
	private String currentPhoneRegion;
	private String currentPhoneRegionValue;

	private static PhoneManagerUtil _intance;

	private PhoneManagerUtil(Context context) {
		this._context = context;

		countryCode = SmsMessageFilter.getUserCountry(_context);

		// Initial phone regions
		phoneRegions = new HashMap<String, String>();
		phoneRegionValues = new HashMap<String, String>();

		phoneRegions.put("vn", PHONE_VN);
		phoneRegionValues.put("vn", PHONE_NUM_VALUE);

		currentPhoneRegion = getPhoneRegion();
		currentPhoneRegionValue = getPhoneRegionValue();
	}

	public static PhoneManagerUtil getIntance(Context context) {
		if (_intance == null) {
			_intance = new PhoneManagerUtil(context);
		}
		return _intance;
	}

	private static boolean sLoadingPhones;

	public String getPhoneRegion() {
		if (!phoneRegions.containsKey(this.countryCode)) {
			return "";
		} else {
			return phoneRegions.get(this.countryCode);
		}
	}

	public String getPhoneRegionValue() {
		if (!phoneRegionValues.containsKey(this.countryCode)) {
			return "";
		} else {
			return phoneRegionValues.get(this.countryCode);
		}
	}

	public void setCheckedStates(boolean isChecked) {
		synchronized (Cache.getInstance()) {
			for (PhoneModel phone : Cache.getItems()) {
				phone.setChecked(isChecked);
			}
		}
	}

	public List<PhoneModel> getPhones() {
		synchronized (Cache.getInstance()) {
			return Cache.getItems();
		}
	}

	public boolean isPhonesEmpty() {
		synchronized (Cache.getInstance()) {
			return Cache.isEmpty();
		}
	}

	public void setPhones(List<PhoneModel> phones) {
		synchronized (Cache.getInstance()) {
			Cache.clearItems();
			for (PhoneModel c : phones) {
				Cache.put(c);
			}
		}
	}

	public List<PhoneModel> searchPhones(String keyword) {
		synchronized (Cache.getInstance()) {
			List<PhoneModel> phones = new ArrayList<PhoneModel>();
			for (PhoneModel phone : Cache.getItems()) {
				if (phone.getDisplayName().toUpperCase().contains(keyword)
						|| phone.getNumber().contains(keyword)) {
					phones.add(phone);
				}
			}
			return phones;
		}
	}

	public PhoneModel getPhoneByContact(long contactId) {
		synchronized (Cache.getInstance()) {
			return Cache.getByContactId(contactId);
		}
	}

	public PhoneModel getPhoneByNumber(String phone) {
		synchronized (Cache.getInstance()) {
			return Cache.getByPhoneNumber(phone.replace(currentPhoneRegion,
					currentPhoneRegionValue));
		}
	}

	public void cacheAllThreads() {
		synchronized (Cache.getInstance()) {
			if (sLoadingPhones) {
				return;
			}
			sLoadingPhones = true;
		}

		HashSet<Long> phonesOnDisk = new HashSet<Long>();

		// Query for all phones.
		Cursor c = _context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				PHONE_PROJECTION, null, null,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		try {
			if (c != null) {
				while (c.moveToNext()) {
					long phoneId = c.getLong(PHONE_ID_INDEX);
					phonesOnDisk.add(phoneId);

					// Try to find this thread ID in the cache.
					PhoneModel phone;
					synchronized (Cache.getInstance()) {
						phone = Cache.get(phoneId);
					}

					if (phone == null) {
						phone = new PhoneModel();
						// Fill the data
						fillFromCursor(phone, c);
						try {
							synchronized (Cache.getInstance()) {
								Cache.put(phone);
							}
						} catch (IllegalStateException e) {
							if (!Cache.replace(phone)) {
							}
						}
					} else {
						fillFromCursor(phone, c);
					}
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
			synchronized (Cache.getInstance()) {
				sLoadingPhones = false;
			}
		}

		// Purge the cache of phones that no longer exist on disk.
		Cache.keepOnly(phonesOnDisk);
	}

	public void cacheAllThreads(Cursor cursor) {
		synchronized (Cache.getInstance()) {
			if (sLoadingPhones) {
				return;
			}
			sLoadingPhones = true;
		}

		HashSet<Long> phonesOnDisk = new HashSet<Long>();
		try {
			if (cursor != null) {
				while (cursor.moveToNext()) {
					long phoneId = cursor.getLong(PHONE_ID_INDEX);
					phonesOnDisk.add(phoneId);

					// Try to find this thread ID in the cache.
					PhoneModel phone;
					synchronized (Cache.getInstance()) {
						phone = Cache.get(phoneId);
					}

					if (phone == null) {
						phone = new PhoneModel();
						// Fill the data
						fillFromCursor(phone, cursor);
						try {
							synchronized (Cache.getInstance()) {
								Cache.put(phone);
							}
						} catch (IllegalStateException e) {
							if (!Cache.replace(phone)) {
							}
						}
					} else {
						fillFromCursor(phone, cursor);
					}
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			synchronized (Cache.getInstance()) {
				sLoadingPhones = false;
			}
		}

		// Purge the cache of threads that no longer exist on disk.
		Cache.keepOnly(phonesOnDisk);
	}

	private void fillFromCursor(PhoneModel phone, Cursor c) {
		synchronized (phone) {
			phone.setId(c.getLong(PHONE_ID_INDEX));
			phone.setContactId(c.getLong(PHONE_CONTACT_ID_INDEX));
			phone.setDisplayName(c.getString(PHONE_DISPLAY_NAME_INDEX));
			phone.setNumber(c.getString(PHONE_NUMBER_INDEX).replaceAll(
					"([\\s\\-])", ""));
		}
	}

	private static class Cache {
		private static Cache sInstance = new Cache();

		static Cache getInstance() {
			return sInstance;
		}

		private final List<PhoneModel> mCache;

		private Cache() {
			mCache = new ArrayList<PhoneModel>();
		}

		static void clearItems() {
			synchronized (sInstance) {
				sInstance.mCache.clear();
			}
		}

		static List<PhoneModel> getItems() {
			synchronized (sInstance) {
				return sInstance.mCache;
			}
		}

		static boolean isEmpty() {
			synchronized (sInstance) {
				return sInstance.mCache.isEmpty();
			}
		}

		static PhoneModel get(long phoneId) {
			synchronized (sInstance) {
				for (PhoneModel c : sInstance.mCache) {
					if (c.getId() == phoneId) {
						return c;
					}
				}
			}
			return null;
		}

		static PhoneModel getByContactId(long contactId) {
			synchronized (sInstance) {
				for (PhoneModel c : sInstance.mCache) {
					if (c.getContactId() == contactId) {
						return c;
					}
				}
			}
			return null;
		}

		static PhoneModel getByPhoneNumber(String number) {
			synchronized (sInstance) {
				for (PhoneModel c : sInstance.mCache) {
					if (c.getNumber().equalsIgnoreCase(number)) {
						return c;
					}
				}
			}
			return null;
		}

		static void put(PhoneModel c) {
			synchronized (sInstance) {
				if (sInstance.mCache.contains(c)) {
					throw new IllegalStateException("cache already contains "
							+ c + " phoneId: " + c.getId());
				}
				sInstance.mCache.add(c);
			}
		}

		static boolean replace(PhoneModel c) {
			synchronized (sInstance) {
				if (!sInstance.mCache.contains(c)) {
					return false;
				}

				sInstance.mCache.remove(c);
				sInstance.mCache.add(c);
				return true;
			}
		}

		static void remove(long phoneId) {
			synchronized (sInstance) {
				for (PhoneModel c : sInstance.mCache) {
					if (c.getId() == phoneId) {
						sInstance.mCache.remove(c);
						return;
					}
				}
			}
		}

		static void keepOnly(Set<Long> phoneIds) {
			synchronized (sInstance) {
				Iterator<PhoneModel> iter = sInstance.mCache.iterator();
				while (iter.hasNext()) {
					PhoneModel c = iter.next();
					if (!phoneIds.contains(c.getId())) {
						iter.remove();
					}
				}
			}
		}
	}
}
