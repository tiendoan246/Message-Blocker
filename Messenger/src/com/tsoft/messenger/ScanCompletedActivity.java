package com.tsoft.messenger;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.tsoft.adapter.CustomListAdapter;
import com.tsoft.content.MessageFilterContent;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.util.DatabaseManagerUtil;

public class ScanCompletedActivity extends AppCompatActivity {

	public static final String SPAM_SENDER = "spam_sender";
	public static final String TOTAL_MESSAGE = "total_message";

	private ListView listView;

	private Set<String> phoneFilters;
	private CustomListAdapter adapter;
	private List<SmsModel> spamSender;

	//private SmsMessageManager smsManager;

	// Context menu
	public static final int CONTEXT_ITEM_DELETE = 1;
	public static final int CONTEXT_ITEM_BLOCK = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_completed);

		// Get control
		listView = (ListView) findViewById(R.id.lvScanCompletedSpam);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		try {
			//smsManager = new SmsMessageManager(this);

			// get spam sender
			spamSender = this.getIntent().getParcelableArrayListExtra(
					SPAM_SENDER);
			int total = this.getIntent().getIntExtra(TOTAL_MESSAGE, 0);

			// Set completed title
			ActionBar actionBar = getSupportActionBar();
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setTitle(MessageFormat.format(
					this.getString(R.string.scan_completed_title),
					spamSender.size(), total));

			// Set list adapter
			adapter = new CustomListAdapter(this, spamSender);
			listView.setAdapter(adapter);

			// Register context menu
			this.registerForContextMenu(listView);
		} catch (Exception ex) {
			Log.e("tag", "error", ex);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
		if (v.getId() == R.id.lvScanCompletedSpam) {
			menu.add(Menu.NONE, CONTEXT_ITEM_DELETE, 0,
					this.getString(R.string.context_item_delete));
			menu.add(Menu.NONE, CONTEXT_ITEM_BLOCK, 1,
					this.getString(R.string.context_item_block));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_completed, menu);
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Get extra info about list item that was long-pressed
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Toast toast = null;

		// Perform action according to selected item from context menu
		switch (item.getItemId()) {

		case CONTEXT_ITEM_DELETE:
			try {
				SmsModel sms = (SmsModel) adapter.getItem(menuInfo.position);
				//smsManager.deleteMessage(sms.getId());
				//Delete spam message
				DatabaseManagerUtil.getInstance(this)
				.deleteSpamMessage(sms.getId());
				adapter.removeItem(menuInfo.position);
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

				if (phoneFilters.contains(sms.getPhone())) {
					toast = Toast.makeText(this, this
							.getString(R.string.message_toast_phone_existed),
							Toast.LENGTH_LONG);
					toast.show();
				} else {
					this.phoneFilters.add(sms.getPhone());
					FilterSettingModel setting = new FilterSettingModel();
					setting.setType(MessageFilterContent.SETTING_TYPE_PHONE);
					setting.setValue(sms.getPhone());

					// Insert
					DatabaseManagerUtil.getInstance(this)
							.insertSetting(setting);
					
					toast = Toast.makeText(this, this
							.getString(R.string.message_toast_filter_blocked),
							Toast.LENGTH_LONG);
					toast.show();
				}
			} catch (Exception ex) {
			}
			break;
		}
		return true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
