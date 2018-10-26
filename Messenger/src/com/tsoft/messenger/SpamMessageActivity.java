package com.tsoft.messenger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.tsoft.adapter.SpamMessageAdapter;
import com.tsoft.content.MessageFilterContent;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.SmsMessageFilter;
import com.tsoft.util.SmsMessageManager;

public class SpamMessageActivity extends AppCompatActivity {
	private static final String TAG = "MessagesActivity";

	private ListView listView;
	private BaseAdapter adapter;
	private SmsMessageManager smsManager;

	private Set<String> phoneFilters;

	// Context menu
	public static final int CONTEXT_ITEM_DELETE = 1;
	public static final int CONTEXT_ITEM_BLOCK = 2;
	public static final int CONTEXT_ITEM_UNSPAM = 3;

	// Result activity
	public static final int ACTIVITY_COMPOSE_SMS_RESULT = 10;
	public static final String COMPOSED_RRESULT = "composed_result";
	public static final String COMPOSED_NEW_RESULT = "composed_new_result";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spam_message);

		listView = (ListView) findViewById(R.id.lvMessages);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		// actionBar.setTitle(R.string.navigator_item_spam_message);
		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();
		ab.setTitle(R.string.navigator_item_spam_message);
		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);

		try {
			smsManager = new SmsMessageManager(this);

			// Set list adapter
			// If show all is enable, show thread conversations
			adapter = new SpamMessageAdapter(this);

			// Set listview adapter
			listView.setAdapter(adapter);

			// Register context menu
			this.registerForContextMenu(listView);
		} catch (Exception ex) {
			Log.e(TAG, "error", ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.spam_messages, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
		if (v.getId() == R.id.lvMessages) {
			menu.add(Menu.NONE, CONTEXT_ITEM_DELETE, 0,
					this.getString(R.string.context_item_delete));

			menu.add(Menu.NONE, CONTEXT_ITEM_BLOCK, 1,
					this.getString(R.string.context_item_block));
			menu.add(Menu.NONE, CONTEXT_ITEM_UNSPAM, 2,
					this.getString(R.string.context_item_unspam));
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
				smsManager.deleteMessage(sms.getId());
				((SpamMessageAdapter) adapter).removeItem(menuInfo.position);

				adapter.notifyDataSetChanged();
			} catch (Exception ex) {
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
			}
			break;
		case CONTEXT_ITEM_UNSPAM:
			try {
				// Get sms
				SmsModel sms = (SmsModel) adapter.getItem(menuInfo.position);
				sms.setMessage(sms.getMessage().replace(
						SmsMessageManager.MESSAGE_SPAM_PREFIX, ""));
				sms.setType(SmsMessageManager.MESSAGE_TYPE_INBOX);
				smsManager.insertSms(sms);
				DatabaseManagerUtil.getInstance(this).deleteSpamMessage(
						sms.getId());
				((SpamMessageAdapter) adapter).removeItem(menuInfo.position);
				adapter.notifyDataSetChanged();
				deletePhoneSetting(sms.getPhone());
			} catch (Exception ex) {
			}
			break;
		}
		return true;
	}

	private void deletePhoneSetting(String value) {
		String original = SmsMessageFilter.getIntance(this).FormatPhoneNumber(
				value);
		DatabaseManagerUtil.getInstance(this).deleteSettingByValue(value);
		DatabaseManagerUtil.getInstance(this).deleteSettingByValue(original);
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
}
