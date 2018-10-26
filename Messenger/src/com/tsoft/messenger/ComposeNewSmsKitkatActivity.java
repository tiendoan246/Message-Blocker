package com.tsoft.messenger;

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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tsoft.adapter.MessagesListAdapter;
import com.tsoft.adapter.PhoneAutocompleteAdapter;
import com.tsoft.control.ContactsCompletionView;
import com.tsoft.control.TokenCompleteTextView.TokenClickStyle;
import com.tsoft.control.TokenCompleteTextView.TokenListener;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.util.ConstantUtil;
import com.tsoft.util.PhoneManagerUtil;
import com.tsoft.util.SmsMessageManager;

public class ComposeNewSmsKitkatActivity extends AppCompatActivity implements
		OnClickListener, TokenListener<PhoneModel> {
	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	public static final String SENDER_NAME = "sender_name";
	public static final String THREAD_ID = "thread_id";
	public static final String CONTENT_FORWARD = "content_forward";

	public static final int PICK_CONTACT_REQUEST = 11;

	// Sending message info
	private static final String SENT = "sent";
	private static final String DELIVERED = "delivered";

	private Button btnSend, btnAddRecipients;
	private EditText inputMsg;
	private ContactsCompletionView autoCompletedRecipients;

	// Chat messages list adapter
	private MessagesListAdapter adapter;
	private List<SmsModel> listMessages = new ArrayList<SmsModel>();
	private ListView listViewMessages;
	private Map<String, String> recipientsPhone = null;

	private PhoneAutocompleteAdapter mAdapter;
	private SmsMessageManager messageManager;
	// URI
	private Uri sentUri = Uri.parse(ConstantUtil.SMS_SENT_URI);
	private boolean isComposed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose_new_sms);

		try {
			ActionBar actionBar = getSupportActionBar();
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setTitle(R.string.compose_message_new_title);

			btnSend = (Button) findViewById(R.id.btnSendNew);
			btnAddRecipients = (Button) findViewById(R.id.btnAddRecipients);
			inputMsg = (EditText) findViewById(R.id.inputMsgNew);
			listViewMessages = (ListView) findViewById(R.id.list_view_messages_new);
			autoCompletedRecipients = (ContactsCompletionView) findViewById(R.id.etAddRecipents);

			messageManager = new SmsMessageManager(this);
			recipientsPhone = new HashMap<String, String>();

			// Set message title

			String contentForward = getIntent().getStringExtra(CONTENT_FORWARD);
			if (contentForward != null) {
				inputMsg.setText(contentForward);
			}

			// load phone
			try {
				if (PhoneManagerUtil.getIntance(this).isPhonesEmpty()) {
					PhoneManagerUtil.getIntance(this).cacheAllThreads();
				}
			} catch (Exception ex) {
				Log.e(TAG, ex.getMessage());
			}

			// Set onclick listener
			btnSend.setOnClickListener(this);
			btnAddRecipients.setOnClickListener(this);
			autoCompletedRecipients.setTokenListener(this);
			autoCompletedRecipients.setTokenClickStyle(TokenClickStyle.Delete);
			autoCompletedRecipients.performBestGuess(false);

			adapter = new MessagesListAdapter(this, listMessages);
			listViewMessages.setAdapter(adapter);

			// Adapter
			mAdapter = new PhoneAutocompleteAdapter(this);
			autoCompletedRecipients.setAdapter(mAdapter);
			autoCompletedRecipients.allowDuplicates(false);
			autoCompletedRecipients.allowCollapse(true);
			autoCompletedRecipients
					.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Appending message to list view
	 * */
	private void appendMessage(final SmsModel m) {
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
						adapter.updateData(m, SmsMessageManager.STATUS_FAILED);

						// Update message
						m.setStatus(SmsMessageManager.STATUS_FAILED);
						messageManager.updateSms(m, sentUri);
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						// Update status
						adapter.updateData(m, SmsMessageManager.STATUS_FAILED);

						// Update message
						m.setStatus(SmsMessageManager.STATUS_FAILED);
						messageManager.updateSms(m, sentUri);
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						// Update status
						adapter.updateData(m, SmsMessageManager.STATUS_FAILED);

						// Update message
						m.setStatus(SmsMessageManager.STATUS_FAILED);
						messageManager.updateSms(m, sentUri);
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						// Update status
						adapter.updateData(m, SmsMessageManager.STATUS_FAILED);

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
					adapter.updateData(m, SmsMessageManager.STATUS_COMPLETE);

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
			smsManager.sendTextMessage(m.getPhone(), null, m.getMessage(),
					sentPI, deliverPI);
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
					Toast.LENGTH_SHORT).show();
			ex.printStackTrace();
		}
		listMessages.add(m);

		adapter.notifyDataSetChanged();

		// Update status
		adapter.updateData(m, SmsMessageManager.STATUS_PENDING);
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
	public void onClick(View v) {
		Toast toast = null;
		switch (v.getId()) {
		case R.id.btnSendNew:
			String str = autoCompletedRecipients.getText().toString();
			if (str == null || str.length() == 0) {
				toast = Toast
						.makeText(
								this,
								this.getString(R.string.compose_message_send_recipents_required),
								Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			String msg = inputMsg.getText().toString();
			if (msg == null || msg.length() == 0) {
				toast = Toast.makeText(this,
						this.getString(R.string.compose_message_send_required),
						Toast.LENGTH_SHORT);
				toast.show();
			} else {
				Set<String> phoneDisplayNames = recipientsPhone.keySet();
				for (String name : phoneDisplayNames) {
					SmsModel sms = new SmsModel();
					sms.setPhone(recipientsPhone.get(name));
					sms.setMessage(inputMsg.getText().toString());

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
				}
				recipientsPhone.clear();
				inputMsg.setText("");
				isComposed = true;
			}
			break;
		case R.id.btnAddRecipients:
			Intent i = new Intent(this, ContactSelectActivity.class);
			startActivityForResult(i, PICK_CONTACT_REQUEST);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
			ArrayList<PhoneModel> phonesResult = data
					.getParcelableArrayListExtra(ContactSelectActivity.CONTACT_RESULT);
			for (PhoneModel phone : phonesResult) {
				if (!recipientsPhone.containsKey(phone.getDisplayName())) {
					autoCompletedRecipients.addObject(phone);
				}
			}
		}
	}

	@Override
	public void onTokenAdded(PhoneModel arg0) {
		if (!recipientsPhone.containsKey(arg0.getDisplayName())) {
			recipientsPhone.put(arg0.getDisplayName(), arg0.getNumber());
		}
	}

	@Override
	public void onTokenRemoved(PhoneModel arg0) {
		if (recipientsPhone.containsKey(arg0.getDisplayName())) {
			recipientsPhone.remove(arg0.getDisplayName());
		}
	}
}
