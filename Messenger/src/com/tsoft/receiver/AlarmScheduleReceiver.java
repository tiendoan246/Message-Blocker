package com.tsoft.receiver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.tsoft.datamodel.MessageScheduleModel;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.util.ConstantUtil;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.NotificationManagerUtil;
import com.tsoft.util.PhoneManagerUtil;
import com.tsoft.util.ScheduleTaskManagerUtil;
import com.tsoft.util.SmsMessageManager;

public class AlarmScheduleReceiver extends BroadcastReceiver {

	// Sending message info
	private static final String SENT = "sent";
	private static final String DELIVERED = "delivered";

	// Message manager
	private SmsMessageManager messageManager;
	private Map<Long, Integer> messageResult;

	// URI
	private Uri sentUri = Uri.parse(ConstantUtil.SMS_SENT_URI);

	@Override
	public void onReceive(Context context, Intent intent) {

		// Create message manager instance
		messageManager = new SmsMessageManager(context);
		messageResult = new HashMap<Long, Integer>();

		// Get first schedule message that have close date with the current
		MessageScheduleModel messageSchedule = DatabaseManagerUtil.getInstance(
				context).getFirstMessageScheduleSending();

		if (messageSchedule != null) {
			// Cancel schedule
			ScheduleTaskManagerUtil.getInstance(context).cancel(
					messageSchedule.getId());

			// Get phone destinations
			List<PhoneModel> phones = getDestinations(context,
					messageSchedule.getDestinationAddress());
			StringBuilder builder = new StringBuilder();
			// Sent messages to destination
			for (PhoneModel phoneModel : phones) {
				SmsModel sms = new SmsModel();
				sms.setMessage(messageSchedule.getMessage());
				sms.setPhone(phoneModel.getNumber());
				sms.setRead(SmsMessageManager.MESSAGE_IS_READ);
				sms.setSeen(SmsMessageManager.MESSAGE_IS_SEEN);
				sms.setType(SmsMessageManager.MESSAGE_TYPE_SENT);
				sms.setStatus(SmsMessageManager.STATUS_PENDING);
				sms.setDate(new Date().getTime());

				// Insert message to sent
				Uri uri = messageManager.insertSms(sms, sentUri);
				long smsId = ContentUris.parseId(uri);

				builder.append(smsId);
				builder.append(";");

				// Set message ID
				sms.setId(smsId);

				// Add message status
				messageResult.put(smsId, SmsMessageManager.STATUS_PENDING);

				// Send message
				sendMessage(context, sms);
			}

			// Delete schedule
			DatabaseManagerUtil.getInstance(context).delete(
					messageSchedule.getId());

			Set<Long> ids = messageResult.keySet();

			// Show notifications
			if (ids.size() > 0) {
				NotificationManagerUtil.getInstance(context)
						.showNotificationOnly(messageSchedule,
								builder.toString(), phones.size(), Color.GREEN);
			}
		}

		// Start the new queue schedule
		ScheduleTaskManagerUtil.getInstance(context).createSchedule();
	}

	private List<PhoneModel> getDestinations(Context context, String destination) {
		List<PhoneModel> phones = new ArrayList<PhoneModel>();
		String args[] = destination.split(";");
		for (int i = 0; i < args.length; i++) {
			PhoneModel phoneModel = PhoneManagerUtil.getIntance(context)
					.getPhoneByNumber(args[i]);
			phones.add(phoneModel);
		}
		return phones;
	}

	/**
	 * Sending messages
	 * */
	private void sendMessage(Context context, final SmsModel m) {
		try {
			Intent sentIntent = new Intent(SENT);
			/* Create Pending Intents */
			PendingIntent sentPI = PendingIntent.getBroadcast(
					context.getApplicationContext(), 0, sentIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			Intent deliveryIntent = new Intent(DELIVERED);

			PendingIntent deliverPI = PendingIntent.getBroadcast(
					context.getApplicationContext(), 0, deliveryIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			/* Register for SMS send action */
			context.getApplicationContext().registerReceiver(
					new BroadcastReceiver() {

						@Override
						public void onReceive(Context context, Intent intent) {

							switch (getResultCode()) {
							case Activity.RESULT_OK:
								// Refresh conversation
								break;
							case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
								// Update status
								if (messageResult.containsKey(m.getId())) {
									messageResult.remove(m.getId());
									messageResult.put(
											m.getId(),
											SmsManager.RESULT_ERROR_GENERIC_FAILURE);
								}

								// Update message
								m.setStatus(SmsMessageManager.STATUS_FAILED);
								messageManager.updateSms(m, sentUri);
								break;
							case SmsManager.RESULT_ERROR_RADIO_OFF:
								// Update status
								if (messageResult.containsKey(m.getId())) {
									messageResult.remove(m.getId());
									messageResult.put(m.getId(),
											SmsManager.RESULT_ERROR_RADIO_OFF);
								}

								// Update message
								m.setStatus(SmsMessageManager.STATUS_FAILED);
								messageManager.updateSms(m, sentUri);
								break;
							case SmsManager.RESULT_ERROR_NULL_PDU:
								// Update status
								if (messageResult.containsKey(m.getId())) {
									messageResult.remove(m.getId());
									messageResult.put(m.getId(),
											SmsManager.RESULT_ERROR_NULL_PDU);
								}

								// Update message
								m.setStatus(SmsMessageManager.STATUS_FAILED);
								messageManager.updateSms(m, sentUri);
								break;
							case SmsManager.RESULT_ERROR_NO_SERVICE:
								// Update status
								if (messageResult.containsKey(m.getId())) {
									messageResult.remove(m.getId());
									messageResult.put(m.getId(),
											SmsManager.RESULT_ERROR_NO_SERVICE);
								}

								// Update message
								m.setStatus(SmsMessageManager.STATUS_FAILED);
								messageManager.updateSms(m, sentUri);
								break;
							}
						}

					}, new IntentFilter(SENT));
			/* Register for Delivery event */
			context.getApplicationContext().registerReceiver(
					new BroadcastReceiver() {

						@Override
						public void onReceive(Context context, Intent intent) {
							// Delivered
							// Update status
							if (messageResult.containsKey(m.getId())) {
								messageResult.remove(m.getId());
								messageResult.put(m.getId(),
										SmsMessageManager.STATUS_COMPLETE);
							}

							// Update message
							m.setDate(new Date().getTime());
							m.setStatus(SmsMessageManager.STATUS_COMPLETE);
							messageManager.updateSms(m, sentUri);
						}

					}, new IntentFilter(DELIVERED));

			/* Send SMS */
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(m.getPhone(), null, m.getMessage(),
					sentPI, deliverPI);
		} catch (Exception ex) {
			Toast.makeText(context.getApplicationContext(),
					ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
			ex.printStackTrace();
		}
	}
}
