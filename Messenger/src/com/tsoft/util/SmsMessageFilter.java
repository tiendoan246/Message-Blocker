package com.tsoft.util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.tsoft.content.MessageFilterContent;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.messenger.R;

public class SmsMessageFilter {

	// VN +84|0|1|9xxxxxxxxxx|x
	private static final String PHONE_REGULAR_EXPRESSION_VN = "(\\+?([0]|84){1}[19]{1}[0-9]{8,9})";

	// US 18005551212 1.800.555.1212 1-800-555-1212 (800)555-1212 8005551212
	// 800-555-1212 800.555.1212 5551212 555-1212 555.1212
	private static final String PHONE_REGULAR_EXPRESSION_US = "1?[-\\. ]?(\\(\\d{3}\\)?[-\\. ]?|\\d{3}?[-\\. ]?)?\\d{3}?[-\\. ]?\\d{4}";

	// 07222 555555 | (07222) 555555 | +44 7222 555 555
	private static final String PHONE_REGULAR_EXPRESSION_UK = "(\\+44\\s?7\\d{3}|\\(?07\\d{3}\\)?)\\s?\\d{3}\\s?\\d{3}";

	// 07222 555555 | (07222) 555555 | +44 7222 555 555
	private static final String PHONE_REGULAR_EXPRESSION_AU = "({0,1}((0|\\+61)(2|4|3|7|8)){0,1}\\){0,1}(\\ |-){0,1}[0-9]{2}(\\ |-){0,1}[0-9]{2}(\\ |-){0,1}[0-9]{1}(\\ |-){0,1}[0-9]{3}";

	// Email
	private static final String EMAIL_REGULAR_EXPRESSION = "([A-Za-z_]+[A-Za-z_\\-0-9\\.]*@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}))";

	private Map<String, String> regularsExp = new HashMap<String, String>();

	private static SmsMessageFilter _instance;
	private Context _context;
	private String phoneRegion;
	private String phoneRegionValue;

	private SmsMessageFilter(Context context) {
		this._context = context;

		regularsExp.put("us", PHONE_REGULAR_EXPRESSION_US);
		regularsExp.put("vn", PHONE_REGULAR_EXPRESSION_VN);
		regularsExp.put("gb", PHONE_REGULAR_EXPRESSION_UK);
		regularsExp.put("au", PHONE_REGULAR_EXPRESSION_AU);

		// Set phone region
		phoneRegion = PhoneManagerUtil.getIntance(context).getPhoneRegion();
		phoneRegionValue = PhoneManagerUtil.getIntance(context)
				.getPhoneRegionValue();
	}

	public static SmsMessageFilter getIntance(Context context) {
		if (_instance == null) {
			_instance = new SmsMessageFilter(context);
		}
		return _instance;
	}

	public String FormatPhoneNumber(String phone) {
		// If then replace the region values
		return phone.replace(phoneRegion, phoneRegionValue);
	}

	public boolean filterMessageByPhone(String phoneNumber) {
		boolean isSpam = false;

		// Load settings
		List<FilterSettingModel> settings = DatabaseManagerUtil.getInstance(
				this._context).getSettingsByType(
				MessageFilterContent.SETTING_TYPE_PHONE);

		// If then replace the region values
		String phone = phoneNumber.replace(phoneRegion, phoneRegionValue);

		for (FilterSettingModel setting : settings) {
			if (setting.getValue().equalsIgnoreCase(phoneNumber)
					|| setting.getValue().equalsIgnoreCase(phone)) {
				isSpam = true;
			}
		}

		return isSpam;
	}

	public boolean filterMessageByContent(String message) {
		// Load settings
		List<FilterSettingModel> settings = DatabaseManagerUtil.getInstance(
				this._context).getSettingsByType(
				MessageFilterContent.SETTING_TYPE_CONTENT);

		String countryCode = getUserCountry(this._context);
		String str = "";
		Pattern regex = null;

		for (FilterSettingModel setting : settings) {
			str = setting.getValue();

			// If PHONE pattern setting
			if (str.equalsIgnoreCase(this._context
					.getString(R.string.setting_pre_phone_regex))) {
				if (countryCode != null && regularsExp.containsKey(countryCode)) {
					str = regularsExp.get(countryCode);
				} else {
					// Default is US
					str = PHONE_REGULAR_EXPRESSION_US;
				}
			} else if (str.equalsIgnoreCase(this._context
					.getString(R.string.setting_pre_email_regex))) {
				str = EMAIL_REGULAR_EXPRESSION;
			}

			regex = Pattern.compile(str, Pattern.CASE_INSENSITIVE);

			// Find the pattern M2
			Matcher matcher = regex.matcher(message);
			while (matcher.find()) {
				return true;
			}
		}

		return false;
	}

	public static String getUserCountry(Context context) {
		try {
			final TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			final String simCountry = tm.getSimCountryIso();
			if (simCountry != null && simCountry.length() == 2) {
				return simCountry.toLowerCase(Locale.US);
			} else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
				String networkCountry = tm.getNetworkCountryIso();
				if (networkCountry != null && networkCountry.length() == 2) {
					return networkCountry.toLowerCase(Locale.US);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
}
