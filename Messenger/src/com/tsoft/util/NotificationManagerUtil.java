package com.tsoft.util;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;

import com.tsoft.content.MessageInbox;
import com.tsoft.datamodel.MessageScheduleModel;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.messenger.ComposeSmsActivity;
import com.tsoft.messenger.ComposeSmsKitkatActivity;
import com.tsoft.messenger.R;

public class NotificationManagerUtil {

	private static final int REQUEST_CODE = 0;

	private static Map<String, Integer> notifications = new HashMap<String, Integer>();
	private static Map<String, Integer> notificationsMessageCount = new HashMap<String, Integer>();

	private static NotificationManagerUtil _intance;
	private Context _context;

	private NotificationManagerUtil(Context context) {
		this._context = context;
	}

	public static NotificationManagerUtil getInstance(Context context) {
		if (_intance == null) {
			_intance = new NotificationManagerUtil(context);
		}
		return _intance;
	}

	public void showNotification(SmsMessageManager messageManager,
			SmsMessage sms) {
		try {
			// Get sender
			String phone = sms.getDisplayOriginatingAddress();
			PhoneModel phoneModel = PhoneManagerUtil.getIntance(this._context)
					.getPhoneByNumber(phone);

			// Notification ID
			int notifiId = -1;
			int messageCount = 0;

			// Get notification if existed
			if (notifications.containsKey(sms.getDisplayOriginatingAddress())) {
				notifiId = notifications
						.get(sms.getDisplayOriginatingAddress());
			}
			if (notificationsMessageCount.containsKey(sms
					.getDisplayOriginatingAddress())) {
				messageCount = notificationsMessageCount.get(sms
						.getDisplayOriginatingAddress());
			}

			// If new
			if (notifiId == -1) {
				Random ran = new Random();
				notifiId = ran.nextInt(1000);
			}

			// Set message count
			++messageCount;

			// Put notification ID
			if (!notifications.containsKey(sms.getDisplayOriginatingAddress())) {
				notifications.put(sms.getDisplayOriginatingAddress(), notifiId);
			}
			// Put notification message count
			if (!notificationsMessageCount.containsKey(sms
					.getDisplayOriginatingAddress())) {
				notificationsMessageCount.put(
						sms.getDisplayOriginatingAddress(), messageCount);
			}

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					this._context)
					.setSmallIcon(R.drawable.ic_launcher)
					.setNumber(messageCount)
					.setContentTitle(
							phoneModel != null ? phoneModel.getDisplayName()
									: sms.getDisplayOriginatingAddress())
					.setContentText(sms.getDisplayMessageBody())
					.setLights(Color.BLUE, 3000, 1000);

			// If conversation
			Class<?> _class;
			if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				_class = ComposeSmsActivity.class;
			} else {
				_class = ComposeSmsKitkatActivity.class;
			}

			// Insert message
			SmsModel smsModel = new SmsModel();
			smsModel.setDate(new Date().getTime());
			smsModel.setMessage(sms.getDisplayMessageBody());
			smsModel.setPhone(sms.getDisplayOriginatingAddress());
			smsModel.setRead(SmsMessageManager.MESSAGE_IS_NOT_READ);
			smsModel.setSeen(SmsMessageManager.MESSAGE_IS_NOT_SEEN);
			smsModel.setType(SmsMessageManager.MESSAGE_TYPE_INBOX);

			// Insert message
			Uri idUri = messageManager.insertSms(smsModel);
			long smsId = ContentUris.parseId(idUri);

			Intent notificationIntent = new Intent(this._context, _class);
			notificationIntent.putExtra(ConstantUtil.NOTIFICATION_ID_KEY,
					notifiId);
			notificationIntent.putExtra(ConstantUtil.NOTIFICATION_PHONE_KEY,
					sms.getDisplayOriginatingAddress());
			notificationIntent.putExtra(
					ConstantUtil.NOTIFICATION_MESSAGE_ID_KEY, smsId);
			notificationIntent.putExtra(ConstantUtil.ACTIVITY_ACTION_TYPE,
					ConstantUtil.NOTIFICATION_ACTION);
			notificationIntent.setAction(Long.toString(System
					.currentTimeMillis()));

			PendingIntent contentIntent = PendingIntent.getActivity(
					this._context, REQUEST_CODE, notificationIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(contentIntent);

			// Add as notification
			NotificationManager manager = (NotificationManager) this._context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			manager.notify(notifiId, builder.build());

			// Play notification sound
			playBeep();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressLint("InlinedApi")
	public void showNotificationOnLockScreen(SmsMessageManager messageManager) {
		try {
			// Get unread messages
			int unreadCount = messageManager.getUnreadMessagesCount();
			String content = MessageFormat.format(this._context
					.getString(R.string.notification_new_messates_content),
					unreadCount);

			// Notification ID
			int notifiId = 0;

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					this._context)
					.setSmallIcon(R.drawable.ic_launcher)
					.setNumber(unreadCount)
					.setContentTitle(
							this._context
									.getString(R.string.notification_new_messates_title))
					.setContentText(content).setAutoCancel(true)
					.setVisibility(Notification.VISIBILITY_PUBLIC)
					.setLights(Color.BLUE, 3000, 1000);

			Intent notificationIntent = new Intent(this._context,
					MessageInbox.class);
			notificationIntent.setAction(Long.toString(System
					.currentTimeMillis()));

			PendingIntent contentIntent = PendingIntent.getActivity(
					this._context, REQUEST_CODE, notificationIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(contentIntent);

			// Add as notification
			NotificationManager manager = (NotificationManager) this._context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			manager.notify(notifiId, builder.build());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void showNotificationOnly(MessageScheduleModel sms, String ids,
			int messageCount, int nitificationColor) {
		try {
			// Notification ID
			int notifiId = -1;

			// Get destination
			String destination = getDestinations(sms.getDestinationAddress());
			String title = MessageFormat.format("{0}: {1}", this._context
					.getString(R.string.notification_schedule_title),
					destination);

			// Get notification if existed
			if (notifications.containsKey(destination)) {
				notifiId = notifications.get(destination);
			}

			// If new
			if (notifiId == -1) {
				Random ran = new Random();
				notifiId = ran.nextInt(1000);
			}

			// Put notification ID
			if (!notifications.containsKey(destination)) {
				notifications.put(destination, notifiId);
			}

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					this._context).setSmallIcon(R.drawable.ic_launcher)
					.setNumber(messageCount).setContentTitle(title)
					.setContentText(sms.getMessage())
					.setLights(nitificationColor, 3000, 1000);

			// If conversation
			Class<?> _class;
			if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				_class = ComposeSmsActivity.class;
			} else {
				_class = ComposeSmsKitkatActivity.class;
			}

			Intent notificationIntent = new Intent(this._context, _class);
			notificationIntent.putExtra(ConstantUtil.NOTIFICATION_ID_KEY,
					notifiId);
			notificationIntent.putExtra(ConstantUtil.NOTIFICATION_PHONE_KEY,
					sms.getDestinationAddress());
			notificationIntent.putExtra(
					ConstantUtil.NOTIFICATION_MESSAGE_ID_KEY, ids);
			notificationIntent.putExtra(ConstantUtil.ACTIVITY_ACTION_TYPE,
					ConstantUtil.NOTIFICATION_SCHEDULE_ACTION);

			PendingIntent contentIntent = PendingIntent.getActivity(
					this._context, REQUEST_CODE, notificationIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(contentIntent);

			// Add as notification
			NotificationManager manager = (NotificationManager) this._context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			manager.notify(notifiId, builder.build());

			// Play notification sound
			playBeep();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Remove notification
	public void removeNotification(int notifiId, String phoneNumber) {
		NotificationManager manager = (NotificationManager) this._context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(notifiId);
		// remove notification ID
		if (notifications.containsKey(phoneNumber)) {
			notifications.remove(phoneNumber);
		}
		// Remove notification number
		if (notificationsMessageCount.containsKey(phoneNumber)) {
			notificationsMessageCount.remove(phoneNumber);
		}
	}

	public void removeAllNotification() {
		if (notifications != null) {
			NotificationManager manager = (NotificationManager) this._context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Set<String> keys = notifications.keySet();
			for (String key : keys) {
				int id = notifications.get(key);
				manager.cancel(id);
			}
			notifications.clear();
		}
	}

	public void playBeep() {
		try {
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(this._context,
					notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getDestinations(String destination) {
		StringBuilder builder = new StringBuilder();
		String args[] = destination.split(";");
		for (int i = 0; i < args.length; i++) {
			PhoneModel phoneModel = PhoneManagerUtil.getIntance(_context)
					.getPhoneByNumber(args[i]);
			builder.append(phoneModel.getDisplayName());
			if (i < args.length - 1) {
				builder.append(", ");
			}
			if (i == 5 && i < args.length - 1) {
				builder.append("...");
				break;
			}
		}
		return builder.toString();
	}
}
