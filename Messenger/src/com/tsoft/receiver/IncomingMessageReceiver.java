package com.tsoft.receiver;

import java.text.MessageFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.tsoft.datamodel.SmsModel;
import com.tsoft.messenger.R;
import com.tsoft.messenger.SettingActivity;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.NotificationManagerUtil;
import com.tsoft.util.SmsMessageFilter;
import com.tsoft.util.SmsMessageManager;

public class IncomingMessageReceiver extends BroadcastReceiver {

	private static final String SMS_EXTRA_NAME = "pdus";
	private static final String SMS_URI = "content://sms";
	// private static final int REQUEST_CODE = 0;

	// Get the object of SmsManager
	final SmsManager sms = SmsManager.getDefault();

	// Filter settings
	private boolean allowDeleteMessage;
	private boolean phoneFilter;
	private boolean contentFilter;

	@Override
	public void onReceive(Context context, Intent intent) {
		// Retrieves a map of extended data from the intent.
		final Bundle bundle = intent.getExtras();

		// Load filter settings
		loadSettings(context);

		// if (phoneFilter || contentFilter) {
		try {
			if (bundle != null) {
				final Object[] pdusObj = (Object[]) bundle.get(SMS_EXTRA_NAME);

				// Get ContentResolver object for pushing encrypted SMS to
				// incoming folder
				ContentResolver contentResolver = context.getContentResolver();
				SmsMessageManager messageManager = new SmsMessageManager(
						Uri.parse(SMS_URI), contentResolver, context);

				for (int i = 0; i < pdusObj.length; i++) {
					SmsMessage currentMessage = SmsMessage
							.createFromPdu((byte[]) pdusObj[i]);

					boolean isSpam = false;

					// Filter message by phone
					if (phoneFilter) {
						isSpam = SmsMessageFilter
								.getIntance(context)
								.filterMessageByPhone(
										currentMessage
												.getDisplayOriginatingAddress());
					}

					// Filter message by content
					if (contentFilter && !isSpam) {
						isSpam = SmsMessageFilter.getIntance(context)
								.filterMessageByContent(
										currentMessage.getMessageBody());
					}

					// If spam abroad this message
					if (isSpam) {
						this.abortBroadcast();

						// Insert this message
						// If the flag allow delete is enable, just skip
						// this
						if (!allowDeleteMessage) {
							// messageManager.insertSms(contentResolver,
							// currentMessage, isSpam);

							// Spam message, insert to spam table
							SmsModel smsModel = new SmsModel();
							smsModel.setDate(new Date().getTime());
							smsModel.setMessage(MessageFormat.format("{0}{1}",
									SmsMessageManager.MESSAGE_SPAM_PREFIX,
									currentMessage.getMessageBody()));
							smsModel.setPhone(currentMessage
									.getDisplayOriginatingAddress());
							smsModel.setRead(SmsMessageManager.MESSAGE_IS_READ);
							smsModel.setSeen(SmsMessageManager.MESSAGE_IS_SEEN);
							smsModel.setType(SmsMessageManager.MESSAGE_TYPE_INBOX);

							DatabaseManagerUtil.getInstance(context)
									.insertSpamMessage(smsModel);
						}
					} else {
						// Just only one nofitication
						this.abortBroadcast();

						// Get sender address
						NotificationManagerUtil.getInstance(context)
								.showNotification(messageManager,
										currentMessage);
					}
				}
			}
		} catch (Exception e) {
			Log.e("SmsReceiver", "Exception smsReceiver" + e);
		}
		// }
	}

	private void loadSettings(Context context) {
		// Get setting preferences
		final SharedPreferences sharedPref = context
				.getSharedPreferences(
						SettingActivity.MESSAGE_SETTING_ATTRIBUTE,
						Context.MODE_PRIVATE);

		// Get filter settings
		allowDeleteMessage = sharedPref.getBoolean(
				context.getString(R.string.setting_pre_delete_message), false);
		phoneFilter = sharedPref.getBoolean(
				context.getString(R.string.setting_pre_enable_phone_filter),
				false);
		contentFilter = sharedPref.getBoolean(
				context.getString(R.string.setting_pre_enable_content_filter),
				false);
	}
}
