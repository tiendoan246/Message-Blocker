package com.tsoft.service;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.tsoft.content.MessageFilterContent;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.messenger.R;
import com.tsoft.messenger.SettingActivity;
import com.tsoft.receiver.IncomingMessageReceiver;
import com.tsoft.util.DatabaseManagerUtil;

public class MessageService extends Service {

	private IncomingMessageReceiver messageReceiver;
	private IntentFilter intentFilter;
	private static final String PHONE_INITIAL_SETUP = "===SYSTEM_PHONE_REGEX===";
	private static final String FIRST_TIME_STARTUP = "first_time_start_up";

	@Override
	public void onCreate() {
		super.onCreate();

		// Create SMS receiver
		messageReceiver = new IncomingMessageReceiver();

		intentFilter = new IntentFilter();

		// Add intent action filter
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

		// Set intent priority
		intentFilter.setPriority(999);

		// <!-- BroadcastReceiver that listens for incoming SMS messages -->
		registerReceiver(messageReceiver, intentFilter);

		try {
			initialContentSettings();
		} catch (Exception ex) {
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

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Unregister receiver
		unregisterReceiver(messageReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
