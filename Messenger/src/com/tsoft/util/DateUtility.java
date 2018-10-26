package com.tsoft.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateFormat;

public class DateUtility {

	private String locale;
	private Context _context;
	private static DateUtility _instance;

	private DateUtility(Context context) {
		this._context = context;
		locale = SmsMessageFilter.getUserCountry(_context);
		if (locale == null) {
			locale = "vn";
		}
	}

	public static DateUtility getInstance(Context context) {
		if (_instance == null) {
			_instance = new DateUtility(context);
		}
		return _instance;
	}

	public String getDateName(Context context, Date date) {
		return DateFormat.getLongDateFormat(context).format(date);
	}

	public String getTimeName(Context context, Date date) {
		return DateFormat.getTimeFormat(context).format(date);
	}

	public String getDate(String locale) {
		String dateformat = locale.equals("vn") ? ConstantUtil.VN_DATE_FORMAT
				: ConstantUtil.US_DATE_FORMAT;
		SimpleDateFormat dayFormat = new SimpleDateFormat(dateformat,
				Locale.getDefault());

		Calendar calendar = Calendar.getInstance();
		return dayFormat.format(calendar.getTime());
	}

	public String getTime() {
		SimpleDateFormat dayFormat = new SimpleDateFormat("HH:mm:ss",
				Locale.getDefault());

		Calendar calendar = Calendar.getInstance();
		return dayFormat.format(calendar.getTime());
	}

	public String getDateTime(long dateValue) {
		String dateformat = locale.equals("vn") ? ConstantUtil.VN_DATE_FORMAT
				: ConstantUtil.US_DATE_FORMAT;
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat,
				Locale.getDefault());
		Date date = new Date(dateValue);
		return dateFormat.format(date);
	}
}
