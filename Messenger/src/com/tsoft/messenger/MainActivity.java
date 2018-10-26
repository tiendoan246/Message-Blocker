package com.tsoft.messenger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tsoft.content.MessageEventActivity;
import com.tsoft.content.MessageFilterContent;
import com.tsoft.content.MessageInbox;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.dialog.RateItDialogFragment;
import com.tsoft.service.MessageService;
import com.tsoft.util.ContactManagerUtil;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.PhoneManagerUtil;
import com.tsoft.util.SmsMessageFilter;
import com.tsoft.util.SmsMessageManager;

public class MainActivity extends AppCompatActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnQueryTextListener,
		OnNavigationItemSelectedListener {

	private static final int LOADER_ID = 1;
	private static final String NAV_ITEM_ID = "navItemId";
	private static final long DRAWER_CLOSE_DELAY_MS = 250;
	private static final String PHONE_INITIAL_SETUP = "===SYSTEM_PHONE_REGEX===";
	private static final String FIRST_TIME_STARTUP = "first_time_start_up";

	private NavigationView navigationView;
	private DrawerLayout drawerLayout;
	private Toolbar toolbar;
	private int mNavItemId;
	private ProgressDialog progressDialog;

	private boolean phoneFilter;
	private boolean contentFilter;
	// private boolean allowDeleteMessage;

	private List<String> phoneFilterList;

	private SmsMessageManager smsMessageManager;
	private final Handler mDrawerActionHandler = new Handler();

	// Tab content
	private static final int TAB_INBOX = 1;
	private static final int TAB_SPAM = 2;
	private static final int TAB_FILTER = 3;
	private static final int TAB_SCAN = 4;
	private static final int TAB_SCHEDULE = 5;
	private static final int TAB_ABOUT = 6;
	private static final int TAB_SETTING = 7;

	private int currentTabSelected = -1;

	private MessageInbox inbox, spam;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		try {
			initialContentSettings();
		} catch (Exception ex) {
		}

		try {
			// Initial loader
			if (ContactManagerUtil.getIntance(this).isContactsEmpty()) {
				getSupportLoaderManager().initLoader(LOADER_ID, null, this);
			}

			PhoneManagerUtil.getIntance(this).cacheAllThreads();

			// Start the message service
			Intent serviceIntent = new Intent(this, MessageService.class);
			this.startService(serviceIntent);

			setupToolbar();

			// load saved navigation state if present
			if (null == savedInstanceState) {
				mNavItemId = R.id.item_inbox;
			} else {
				mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
			}

			// Initializing NavigationView
			navigationView = (NavigationView) findViewById(R.id.navigation_view);
			navigationView.setNavigationItemSelectedListener(this);

			smsMessageManager = new SmsMessageManager(this);

			// select the correct nav menu item
			navigationView.getMenu().findItem(mNavItemId).setChecked(true);

			// Initializing Drawer Layout and ActionBarToggle
			drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
			ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
					this, drawerLayout, toolbar, R.string.openDrawer,
					R.string.closeDrawer) {

				@Override
				public void onDrawerClosed(View drawerView) {
					// Code here will be triggered once the drawer closes as we
					// dont want anything to happen so we leave this blank
					super.onDrawerClosed(drawerView);
				}

				@Override
				public void onDrawerOpened(View drawerView) {
					// Code here will be triggered once the drawer open as we
					// dont want anything to happen so we leave this blank
					super.onDrawerOpened(drawerView);
				}
			};

			// Setting the actionbarToggle to drawer layout
			drawerLayout.setDrawerListener(actionBarDrawerToggle);
			// calling sync state is necessay or else your hamburger icon wont
			// show up
			actionBarDrawerToggle.syncState();

			int currentVersion = android.os.Build.VERSION.SDK_INT;

			// Check the current version is greater than or equal KITKAT
			if (currentVersion >= Build.VERSION_CODES.KITKAT) {
				showComfirmDefault();
			}

			// Update the selected menu
			navigate(mNavItemId);

			// Show rate this app
			RateItDialogFragment.show(this, getFragmentManager());
		} catch (Exception ex) {
			Log.e("tag", "error", ex);
		}

	}

	private void initialContentSettings() {
		// Load first time startup
		SharedPreferences sharedPref = this
				.getSharedPreferences(
						SettingActivity.MESSAGE_SETTING_ATTRIBUTE,
						Context.MODE_PRIVATE);
		boolean firsttime = sharedPref.getBoolean(FIRST_TIME_STARTUP, false);

		if (!firsttime) {
			// First time
			saveBooleanSetting(
					getString(R.string.setting_pre_enable_phone_filter), true);
			saveBooleanSetting(
					getString(R.string.setting_pre_enable_content_filter), true);
			saveBooleanSetting(FIRST_TIME_STARTUP, true);

			// Load settings
			List<FilterSettingModel> settings = DatabaseManagerUtil
					.getInstance(this).getSettingsByType(
							MessageFilterContent.SETTING_TYPE_CONTENT);
			boolean isFound = false;
			for (FilterSettingModel setting : settings) {
				if (setting.getValue().equalsIgnoreCase(PHONE_INITIAL_SETUP)) {
					isFound = true;
				}
			}
			if (!isFound) {
				FilterSettingModel setting = new FilterSettingModel();
				setting.setType(MessageFilterContent.SETTING_TYPE_CONTENT);
				setting.setValue(PHONE_INITIAL_SETUP);
				DatabaseManagerUtil.getInstance(this).insertSetting(setting);

			}
		}
	}

	private void saveBooleanSetting(String key, boolean value) {
		SharedPreferences sharedPref = this
				.getSharedPreferences(
						SettingActivity.MESSAGE_SETTING_ATTRIBUTE,
						Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	private void loadSettings() {
		phoneFilterList = new ArrayList<String>();
		// Load settings
		List<FilterSettingModel> settings = DatabaseManagerUtil.getInstance(
				this)
				.getSettingsByType(MessageFilterContent.SETTING_TYPE_PHONE);
		for (FilterSettingModel setting : settings) {
			phoneFilterList.add(setting.getValue());
		}
	}

	private void navigate(final int itemId) {
		// Check to see which item was being clicked and perform appropriate
		// action
		Toast toast = null;
		android.support.v4.app.FragmentTransaction fragmentTransaction;
		switch (itemId) {
		case R.id.item_inbox:
			currentTabSelected = TAB_INBOX;
			// Set title
			getSupportActionBar().setTitle(R.string.navigator_item_inbox);

			inbox = new MessageInbox(this, true);
			fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			fragmentTransaction.replace(R.id.frame, inbox);
			fragmentTransaction.commit();
			break;
		case R.id.item_spam_message:
			currentTabSelected = TAB_SPAM;
			// Set title
			getSupportActionBar()
					.setTitle(R.string.navigator_item_spam_message);

			spam = new MessageInbox(this, false);
			fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			fragmentTransaction.replace(R.id.frame, spam);
			fragmentTransaction.commit();
			break;
		case R.id.item_filter:
			currentTabSelected = TAB_FILTER;
			// Set title
			getSupportActionBar().setTitle(R.string.navigator_item_filter);
			MessageFilterContent filter = new MessageFilterContent(this);
			fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			fragmentTransaction.replace(R.id.frame, filter);
			fragmentTransaction.commit();
			break;
		case R.id.item_about:
			currentTabSelected = TAB_ABOUT;
			Intent iAbout = new Intent(this, AboutActivity.class);
			startActivity(iAbout);
			break;
		case R.id.item_setting:
			currentTabSelected = TAB_SETTING;
			Intent iSetting = new Intent(this, SettingActivity.class);
			startActivity(iSetting);
			break;
		case R.id.item_schedule:
			currentTabSelected = TAB_SCHEDULE;
			// Set title
			getSupportActionBar().setTitle(R.string.button_home_schedule);

			MessageEventActivity events = new MessageEventActivity(this);
			fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			fragmentTransaction.replace(R.id.frame, events);
			fragmentTransaction.commit();
			break;
		case R.id.item_scan:
			currentTabSelected = TAB_SCAN;
			// Load settings
			loadSettings(MainActivity.this);
			loadSettings();

			if (phoneFilter || contentFilter) {
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this
						.getString(R.string.scan_inprogress_title));
				progressDialog.setIndeterminate(false);
				progressDialog.setMax(100);
				progressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setCancelable(false);
				progressDialog.show();

				ArrayList<SmsModel> messages = smsMessageManager
						.getInboxMessages(true);
				new ScanMessages().execute(messages.toArray());
			} else {
				toast = Toast
						.makeText(
								this,
								this.getString(R.string.message_toast_scan_not_enable_filter),
								Toast.LENGTH_SHORT);
				toast.show();
			}
			break;
		default:
			currentTabSelected = -1;
			break;
		}
	}

	private void setupToolbar() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Show menu icon
		final ActionBar ab = getSupportActionBar();
		ab.setHomeAsUpIndicator(R.drawable.ic_menu);
		ab.setDisplayHomeAsUpEnabled(true);

		if (toolbar != null)
			toolbar.setTitleTextColor(Color.WHITE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu
				.findItem(R.id.action_search));
		searchView.setOnQueryTextListener(this);

		try {
			EditText txtSearch = ((EditText) searchView
					.findViewById(android.support.v7.appcompat.R.id.search_src_text));
			txtSearch.setHintTextColor(Color.LTGRAY);
			txtSearch.setTextColor(Color.WHITE);
		} catch (Exception ex) {
		}
		return super.onCreateOptionsMenu(menu);
	}

	@SuppressLint("NewApi")
	private void showComfirmDefault() {
		final String myPackageName = getPackageName();
		String phonePackage = Telephony.Sms.getDefaultSmsPackage(this);

		if (phonePackage == null || !phonePackage.equals(myPackageName)) {

			Intent intent = new Intent(
					Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
			intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
					myPackageName);
			try {
				startActivity(intent);
			} catch (Exception ex) {

			}
		}
	}

	private void loadSettings(Context context) {
		// Get setting preferences
		final SharedPreferences sharedPref = context
				.getSharedPreferences(
						SettingActivity.MESSAGE_SETTING_ATTRIBUTE,
						Context.MODE_PRIVATE);

		// Get filter settings
		phoneFilter = sharedPref.getBoolean(
				context.getString(R.string.setting_pre_enable_phone_filter),
				false);
		contentFilter = sharedPref.getBoolean(
				context.getString(R.string.setting_pre_enable_content_filter),
				false);
	}

	private boolean scanMessages(SmsModel sms) {
		boolean isSpam = false;
		// Check if the message is already mark as SPAM
		if (sms.getMessage().contains(SmsMessageManager.MESSAGE_SPAM_PREFIX)) {
			// Delete message from device
			smsMessageManager.deleteMessage(sms.getId());

			// Insert message to the database
			long id = DatabaseManagerUtil.getInstance(this).insertSpamMessage(sms);
			if (id > 0){
				sms.setId(id);
			}
			return false;
		}
		if (phoneFilter) {
			String phoneNumber = SmsMessageFilter.getIntance(this)
					.FormatPhoneNumber(sms.getPhone());
			if (phoneFilterList.contains(phoneNumber)
					|| phoneFilterList.contains(sms.getPhone())) {
				isSpam = true;
			}
		}
		if (contentFilter && !isSpam) {
			isSpam = SmsMessageFilter.getIntance(this).filterMessageByContent(
					sms.getMessage());
		}
		if (isSpam) {
			// Delete message from device
			smsMessageManager.deleteMessage(sms.getId());

			// Insert message to the database
			sms.setMessage(MessageFormat.format("{0}{1}",
					SmsMessageManager.MESSAGE_SPAM_PREFIX, sms.getMessage()));
			long id = DatabaseManagerUtil.getInstance(this).insertSpamMessage(sms);
			if (id > 0){
				sms.setId(id);
			}
		}
		return isSpam;
	}

	private class ScanMessages extends AsyncTask<Object, Integer, Boolean> {

		private ArrayList<SmsModel> spamMessages = new ArrayList<SmsModel>();
		private int total = 0;

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (spamMessages.size() > 0) {
				// Start completed activity
				Intent intent = new Intent(MainActivity.this,
						ScanCompletedActivity.class);
				Bundle bundle = new Bundle();
				// bundle.putStringArrayList(ScanCompletedActivity.SPAM_SENDER,
				// spamMessages);
				bundle.putParcelableArrayList(
						ScanCompletedActivity.SPAM_SENDER, spamMessages);
				bundle.putInt(ScanCompletedActivity.TOTAL_MESSAGE, total);
				intent.putExtras(bundle);
				// Start
				startActivity(intent);
			} else {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						new ContextThemeWrapper(MainActivity.this,
								android.R.style.Theme_Holo_Light_Dialog));

				// set title
				alertDialogBuilder.setTitle(MainActivity.this
						.getString(R.string.scan_completed_dialog_title));

				// set dialog message
				alertDialogBuilder
						.setMessage(
								MainActivity.this
										.getString(R.string.scan_completed_dialog_content))
						.setCancelable(false)
						.setPositiveButton(
								MainActivity.this.getString(R.string.button_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}
			progressDialog.dismiss();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int current = values[0];
			total = values[1];

			int percentage = (int) ((100 * (float) current) / (float) total);

			// Set percentage for scanning
			progressDialog.setProgress(percentage);
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			try {
				for (int i = 0; i < params.length; i++) {
					SmsModel sms = (SmsModel) params[i];
					boolean isSpam = scanMessages(sms);
					if (isSpam) {
						spamMessages.add(sms);
					}

					// We have processed item 'i' out of 'count'
					publishProgress((i + 1), params.length);
				}
				return true;
			} catch (Exception e) {
				Log.e("tag", "error", e);
				return false;
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri CONTENT_URI = Contacts.CONTENT_URI;
		return new CursorLoader(
				this,
				CONTENT_URI,
				ContactManagerUtil.ALL_CONTACTS_PROJECTION,
				null,
				null,
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
						: ContactsContract.Contacts.DISPLAY_NAME);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		ContactManagerUtil.getIntance(this).cacheAllThreads(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		switch (currentTabSelected) {
		case TAB_INBOX:
			inbox.applySearch(arg0);
			break;
		case TAB_SPAM:
			spam.applySearch(arg0);
			break;
		}
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		return false;
	}

	@Override
	public boolean onNavigationItemSelected(final MenuItem menuItem) {
		// update highlighted item in the navigation menu
		menuItem.setChecked(true);
		mNavItemId = menuItem.getItemId();

		// allow some time after closing the drawer before performing real
		// navigation
		// so the user can see what is happening
		drawerLayout.closeDrawer(GravityCompat.START);
		mDrawerActionHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				navigate(menuItem.getItemId());
			}
		}, DRAWER_CLOSE_DELAY_MS);
		return true;
	}
}
