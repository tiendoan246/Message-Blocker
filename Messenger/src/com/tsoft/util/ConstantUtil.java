package com.tsoft.util;

public class ConstantUtil {
	// Notification constant
	public static final String NOTIFICATION_ID_KEY = "notification_id_key";
	public static final String NOTIFICATION_PHONE_KEY = "notification_phone_key";
	public static final String NOTIFICATION_MESSAGE_ID_KEY = "notification_message_id";

	// Activity type
	public static final String ACTIVITY_ACTION_TYPE = "activity_action_type";

	// Action values
	public static final int NOTIFICATION_ACTION = 1;
	public static final int THREAD_DETAIL_MESSAGE_ACTION = 2;
	public static final int MESSAGE_DETAIL_ACTION = 3;
	public static final int NOTIFICATION_SCHEDULE_ACTION = 4;

	// Message SMS URI
	public static final String SMS_INBOX_URI = "content://sms/inbox";
	public static final String SMS_SENT_URI = "content://sms/sent";
	public static final String SMS_MESSAGES_URI = "content://sms";

	// Date format
	public static final String US_DATE_FORMAT = "MM-dd-yyyy HH:mm:ss";
	public static final String VN_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

	// Schedule
	public static final String SCHEDULE_ID = "schedule_id";

	// SMS status
	public static final int SMS_SENDING = 1;
	public static final int SMS_DELIVERED = 2;
	public static final int SMS_SENT = 99;
}
