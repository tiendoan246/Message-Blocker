package com.tsoft.messenger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tsoft.adapter.MessagesListAdapter;
import com.tsoft.content.MessageFilterContent;
import com.tsoft.content.MessageInbox;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.util.ConstantUtil;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.NotificationManagerUtil;
import com.tsoft.util.PhoneManagerUtil;
import com.tsoft.util.SmsMessageManager;

public class ComposeSmsActivity extends AppCompatActivity implements
		OnClickListener {

	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	public static final String SENDER_NAME = "sender_name";
	public static final String THREAD_ID = "thread_id";
	public static final String MESSAGE_ID = "message_id";
	public static final String THREAD_UNREAD_COUNT = "thread_unread_count";

	// Sending message info
	private static final String SENT = "sent";
	private static final String DELIVERED = "delivered";

	// Filter list
	private Set<String> phoneFilters;

	// Context menu
	public static final int CONTEXT_ITEM_DELETE = 1;
	public static final int CONTEXT_ITEM_BLOCK = 2;
	public static final int CONTEXT_ITEM_FORWARD = 3;
	public static final int CONTEXT_ITEM_RESEND = 4;

	private Button btnSend;
	private EditText inputMsg;

	// Chat messages list adapter
	private MessagesListAdapter adapter;
	private List<SmsModel> listMessages;
	private ListView listViewMessages;

	private SmsMessageManager messageManager;

	private String sender;
	private long threadId;
	private long messageId;

	// URI
	private Uri sentUri = Uri.parse(ConstantUtil.SMS_SENT_URI);
	private boolean isComposed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose_sms);

		try {
			ActionBar actionBar = getSupportActionBar();
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);

			btnSend = (Button) findViewById(R.id.btnSend);
			inputMsg = (EditText) findViewById(R.id.inputMsg);
			listViewMessages = (ListView) findViewById(R.id.list_view_messages);

			messageManager = new SmsMessageManager(this);

			// Set onclick listener
			btnSend.setOnClickListener(this);
			// tvSender.setOnClickListener(this);

			// Getting the person name from previous screen
			Intent i = getIntent();
			threadId = i.getLongExtra(THREAD_ID, 0);
			sender = i.getStringExtra(SENDER_NAME);
			int unreadCount = i.getIntExtra(THREAD_UNREAD_COUNT, 0);
			messageId = i.getLongExtra(MESSAGE_ID, -1);

			// Get action type
			int actionType = i.getIntExtra(ConstantUtil.ACTIVITY_ACTION_TYPE,
					-1);

			switch (actionType) {
			case ConstantUtil.NOTIFICATION_ACTION:
				// Get notification ID
				int notificationId = i.getIntExtra(
						ConstantUtil.NOTIFICATION_ID_KEY, -1);
				long smsId = i.getLongExtra(
						ConstantUtil.NOTIFICATION_MESSAGE_ID_KEY, -1);
				String phoneNumber = i
						.getStringExtra(ConstantUtil.NOTIFICATION_PHONE_KEY);

				// Display notification message
				displayNotification(notificationId, smsId, phoneNumber);
				break;
			case ConstantUtil.THREAD_DETAIL_MESSAGE_ACTION:
				// Display thread message details
				displayThreadMessageDetails(threadId, sender, unreadCount);
				break;
			case ConstantUtil.MESSAGE_DETAIL_ACTION:
				// Display single message details, this is spam and don't need
				// update read status
				if (messageId != -1) {
					displayMessageDetails(messageId, sender);
				}
				break;
			case ConstantUtil.NOTIFICATION_SCHEDULE_ACTION:
				// Get notification ID
				int scheduleNotifiId = i.getIntExtra(
						ConstantUtil.NOTIFICATION_ID_KEY, -1);
				String schedulePhoneNumber = i
						.getStringExtra(ConstantUtil.NOTIFICATION_PHONE_KEY);
				String smsIds = i
						.getStringExtra(ConstantUtil.NOTIFICATION_MESSAGE_ID_KEY);

				// Display notification schedule
				displayNotificationSchedule(scheduleNotifiId, smsIds,
						schedulePhoneNumber);
				break;
			default:
				break;
			}

			adapter = new MessagesListAdapter(this, listMessages);
			listViewMessages.setAdapter(adapter);

			// Register context menu
			this.registerForContextMenu(listViewMessages);
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	private void displayMessageDetails(long messageId, String sender) {
		// Set contact info
		// phone
		// Replace VN region
		if (sender != null) {
			PhoneModel phoneModel = PhoneManagerUtil.getIntance(this)
					.getPhoneByNumber(sender);

			getSupportActionBar().setTitle(
					phoneModel != null ? phoneModel.getDisplayName() : sender);
		}

		SmsModel sms = DatabaseManagerUtil.getInstance(this).getSpamMessage(
				messageId);

		listMessages = new ArrayList<SmsModel>();
		listMessages.add(sms);
	}

	private void displayThreadMessageDetails(long threadId, String sender,
			int unreadCount) {
		// Set contact info
		// phone
		// Replace VN region
		if (sender != null) {
			PhoneModel phoneModel = PhoneManagerUtil.getIntance(this)
					.getPhoneByNumber(sender);

			getSupportActionBar().setTitle(
					phoneModel != null ? phoneModel.getDisplayName() : sender);
		}

		listMessages = messageManager.getMessagesByThreadID(String
				.valueOf(threadId));

		// Update thread message as read
		if (unreadCount > 0) {
			messageManager.updateSmsReadStatusByThread(
					String.valueOf(threadId),
					SmsMessageManager.MESSAGE_IS_READ,
					SmsMessageManager.MESSAGE_IS_SEEN);
		}
	}

	private void displayNotificationSchedule(int notifiId, String smsIds,
			String phoneNumber) {
		listMessages = new ArrayList<SmsModel>();
		// If this start by notification clicked
		if (notifiId != -1) {
			for (String id : smsIds.split(";")) {
				if (id != null && id.length() > 0) {
					SmsModel sms = messageManager.getMessagesByID(id);
					// The conversation is existed
					if (sms != null) {
						listMessages.add(sms);

						// This is a new conversation
						String phone = sms.getPhone();
						PhoneModel phoneModel = PhoneManagerUtil.getIntance(
								this).getPhoneByNumber(phone);

						// tvSender.setText(phoneModel != null ? phoneModel
						// .getDisplayName() : sms.getPhone());

						// Set sender and current thread
						sender = sms.getPhone();
						threadId = sms.getThreadId();

						// Update message status to read
						sms.setRead(SmsMessageManager.MESSAGE_IS_READ);

						// Update message to read status
						messageManager.updateSms(sms,
								Uri.parse(ConstantUtil.SMS_INBOX_URI));
					}
				}
			}

			// Remove notification
			NotificationManagerUtil.getInstance(this).removeNotification(
					notifiId, phoneNumber);
		}
	}

	private void displayNotification(int notifiId, long messageId,
			String phoneNumber) {
		// If this start by notification clicked
		if (notifiId != -1) {
			SmsModel sms = messageManager.getMessagesByID(String
					.valueOf(messageId));
			// The conversation is existed
			if (sms != null) {
				listMessages = messageManager.getMessagesByThreadID(String
						.valueOf(sms.getThreadId()));

				// This is a new conversation
				String phone = sms.getPhone();
				PhoneModel phoneModel = PhoneManagerUtil.getIntance(this)
						.getPhoneByNumber(phone);

				ActionBar actionBar = getSupportActionBar();
				actionBar.setTitle(phoneModel != null ? phoneModel
						.getDisplayName() : sms.getPhone());

				// Set sender and current thread
				sender = sms.getPhone();
				threadId = sms.getThreadId();

				// Update message status to read
				sms.setRead(SmsMessageManager.MESSAGE_IS_READ);
				sms.setSeen(SmsMessageManager.MESSAGE_IS_SEEN);

				// Update message to read status
				//messageManager.updateSms(sms,
				//		Uri.parse(ConstantUtil.SMS_INBOX_URI));
				messageManager.updateSmsReadStatusByThread(
						String.valueOf(threadId),
						SmsMessageManager.MESSAGE_IS_READ,
						SmsMessageManager.MESSAGE_IS_SEEN);
			}

			// Remove notification
			NotificationManagerUtil.getInstance(this).removeNotification(
					notifiId, phoneNumber);
		}
	}

	/**
	 * Appending message to list view
	 * */
	private void appendMessage(final SmsModel m) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				try {
					Intent sentIntent = new Intent(SENT);
					/* Create Pending Intents */
					PendingIntent sentPI = PendingIntent.getBroadcast(
							getApplicationContext(), 0, sentIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);

					Intent deliveryIntent = new Intent(DELIVERED);

					PendingIntent deliverPI = PendingIntent.getBroadcast(
							getApplicationContext(), 0, deliveryIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);
					/* Register for SMS send action */
					registerReceiver(new BroadcastReceiver() {

						@Override
						public void onReceive(Context context, Intent intent) {
							switch (getResultCode()) {
							case Activity.RESULT_OK:
								// Update status
								adapter.updateData(m, ConstantUtil.SMS_SENT);

								// Update message
								m.setStatus(ConstantUtil.SMS_SENT);
								messageManager.updateSms(m, sentUri);
								break;
							case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
								// Update status
								adapter.updateData(m,
										SmsMessageManager.STATUS_FAILED);

								// Update message
								m.setStatus(SmsMessageManager.STATUS_FAILED);
								messageManager.updateSms(m, sentUri);
								break;
							case SmsManager.RESULT_ERROR_RADIO_OFF:
								// Update status
								adapter.updateData(m,
										SmsMessageManager.STATUS_FAILED);

								// Update message
								m.setStatus(SmsMessageManager.STATUS_FAILED);
								messageManager.updateSms(m, sentUri);
								break;
							case SmsManager.RESULT_ERROR_NULL_PDU:
								// Update status
								adapter.updateData(m,
										SmsMessageManager.STATUS_FAILED);

								// Update message
								m.setStatus(SmsMessageManager.STATUS_FAILED);
								messageManager.updateSms(m, sentUri);
								break;
							case SmsManager.RESULT_ERROR_NO_SERVICE:
								// Update status
								adapter.updateData(m,
										SmsMessageManager.STATUS_FAILED);

								// Update message
								m.setStatus(SmsMessageManager.STATUS_FAILED);
								messageManager.updateSms(m, sentUri);
								break;
							}
						}

					}, new IntentFilter(SENT));
					/* Register for Delivery event */
					registerReceiver(new BroadcastReceiver() {

						@Override
						public void onReceive(Context context, Intent intent) {
							// Delivered
							// Update status
							adapter.updateData(m,
									SmsMessageManager.STATUS_COMPLETE);

							// Update message
							m.setDate(new Date().getTime());
							m.setStatus(SmsMessageManager.STATUS_COMPLETE);
							messageManager.updateSms(m, sentUri);

							// Playing device's notification
							playBeep();
						}

					}, new IntentFilter(DELIVERED));

					/* Send SMS */
					SmsManager smsManager = SmsManager.getDefault();
					smsManager.sendTextMessage(m.getPhone(), null,
							m.getMessage(), sentPI, deliverPI);
				} catch (Exception ex) {
					Toast.makeText(getApplicationContext(),
							ex.getMessage().toString(), Toast.LENGTH_SHORT)
							.show();
					ex.printStackTrace();
				}
				if (!listMessages.contains(m)) {
					listMessages.add(m);
				}

				adapter.notifyDataSetChanged();

				// Update status
				adapter.updateData(m, SmsMessageManager.STATUS_PENDING);
			}
		});
	}

	/**
	 * Plays device's default notification sound
	 * */
	public void playBeep() {
		try {
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
		if (v.getId() == R.id.list_view_messages) {
			menu.add(Menu.NONE, CONTEXT_ITEM_DELETE, 0,
					this.getString(R.string.context_item_delete));

			// Get the info on which item was selected
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			// Process for message items
			SmsModel sms = (SmsModel) adapter.getItem(info.position);

			if (sms != null) {
				// If not sender, don't add this
				if (sms.isSender()) {
					menu.add(Menu.NONE, CONTEXT_ITEM_BLOCK, 1,
							this.getString(R.string.context_item_block));
				}
				if (sms.getStatus() == SmsMessageManager.STATUS_FAILED) {
					menu.add(Menu.NONE, CONTEXT_ITEM_RESEND, 3,
							this.getString(R.string.context_item_resend));
				}
			} else {
				menu.add(Menu.NONE, CONTEXT_ITEM_BLOCK, 1,
						this.getString(R.string.context_item_block));
			}
			menu.add(Menu.NONE, CONTEXT_ITEM_FORWARD, 2,
					this.getString(R.string.context_item_forward));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Get extra info about list item that was long-pressed
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();

		// Perform action according to selected item from context menu
		switch (item.getItemId()) {

		case CONTEXT_ITEM_DELETE:
			try {
				// Process for message items
				SmsModel sms = (SmsModel) adapter.getItem(menuInfo.position);
				messageManager.deleteMessage(sms.getId());
				adapter.removeItem(menuInfo.position);

				adapter.notifyDataSetChanged();

				// Make sure conversation will be refreshed after back
				isComposed = true;
			} catch (Exception ex) {
				Log.e(TAG, ex.getMessage());
			}
			break;
		case CONTEXT_ITEM_BLOCK:
			try {
				// Load phone filter settings
				loadSettings();

				// Get sms
				SmsModel sms = (SmsModel) adapter.getItem(menuInfo.position);
				blockPerson(sms.getPhone());
			} catch (Exception ex) {
				Log.e(TAG, ex.getMessage());
			}
			break;
		case CONTEXT_ITEM_FORWARD:
			// Get sms
			SmsModel sms = (SmsModel) adapter.getItem(menuInfo.position);

			if (sms != null) {
				Intent i = new Intent(this, ComposeNewSmsActivity.class);
				i.putExtra(ComposeNewSmsActivity.CONTENT_FORWARD,
						sms.getMessage());
				startActivity(i);
			}
			break;
		case CONTEXT_ITEM_RESEND:
			try {
				// Process for message items
				SmsModel _sms = (SmsModel) adapter.getItem(menuInfo.position);

				appendMessage(_sms);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			break;
		}
		return true;
	}

	private void blockPerson(String sender) {
		Toast toast = null;
		if (phoneFilters.contains(sender)) {
			toast = Toast.makeText(this,
					this.getString(R.string.message_toast_phone_existed),
					Toast.LENGTH_LONG);
			toast.show();
		} else {
			this.phoneFilters.add(sender);
			FilterSettingModel setting = new FilterSettingModel();
			setting.setType(MessageFilterContent.SETTING_TYPE_PHONE);
			setting.setValue(sender);

			// Insert
			DatabaseManagerUtil.getInstance(this).insertSetting(setting);

			toast = Toast.makeText(this,
					this.getString(R.string.message_toast_filter_blocked),
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

	private void loadSettings() {
		// Load settings
		List<FilterSettingModel> settings = DatabaseManagerUtil.getInstance(
				this)
				.getSettingsByType(MessageFilterContent.SETTING_TYPE_PHONE);

		phoneFilters = new HashSet<String>();

		for (FilterSettingModel setting : settings) {
			phoneFilters.add(setting.getValue());
		}
	}

	@Override
	public void onClick(View v) {
		Toast toast = null;
		switch (v.getId()) {
		case R.id.btnSend:
			String msg = inputMsg.getText().toString();
			if (msg == null || msg.length() == 0) {
				toast = Toast.makeText(this,
						this.getString(R.string.compose_message_send_required),
						Toast.LENGTH_SHORT);
				toast.show();
			} else {
				SmsModel sms = new SmsModel();
				sms.setPhone(sender);
				sms.setMessage(inputMsg.getText().toString());
				sms.setThreadId(threadId);

				// Message state
				sms.setRead(SmsMessageManager.MESSAGE_IS_READ);
				sms.setSeen(SmsMessageManager.MESSAGE_IS_SEEN);
				sms.setType(SmsMessageManager.MESSAGE_TYPE_SENT);
				sms.setStatus(SmsMessageManager.STATUS_PENDING);
				sms.setDate(new Date().getTime());

				// Insert message to sent
				Uri uri = messageManager.insertSms(sms, sentUri);
				long smsId = ContentUris.parseId(uri);

				// Set message ID
				sms.setId(smsId);

				appendMessage(sms);
				inputMsg.setText("");
				isComposed = true;
			}
			break;
		// case R.id.tvComposeSender:
		// Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
		// + sender));
		// startActivity(intent);
		// break;
		}
	}

	@Override
	public void onBackPressed() {
		if (isComposed) {
			Intent returnIntent = new Intent();
			returnIntent.putExtra(MessageInbox.COMPOSED_RRESULT, isComposed);
			setResult(RESULT_OK, returnIntent);
		}
		super.onBackPressed();
	}
}
