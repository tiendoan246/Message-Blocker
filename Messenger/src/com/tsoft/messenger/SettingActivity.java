package com.tsoft.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class SettingActivity extends AppCompatActivity implements
		OnClickListener {

	public static final String MESSAGE_SETTING_ATTRIBUTE = "message_settings_attribute";
	public static final String MESSAGE_SETTING_PHONE = "message_settings_phone";

	private CheckBox chkDeleteMessage, chkEnablePhoneFilter, chkEnableContentFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setTitle(R.string.navigator_item_setting);

		chkDeleteMessage = (CheckBox) findViewById(R.id.chkDeleteMessage);
		chkEnableContentFilter = (CheckBox) findViewById(R.id.chkEnableFilterByContent);
		chkEnablePhoneFilter = (CheckBox) findViewById(R.id.chkEnableFilterByPhone);

		// Set the listener
		chkDeleteMessage.setOnClickListener(this);
		chkEnableContentFilter.setOnClickListener(this);
		chkEnablePhoneFilter.setOnClickListener(this);

		// Load settings preferences
		try {
			loadSettings();
		} catch (Exception ex) {
			Log.i("SettingActivity", "Exception " + ex);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chkDeleteMessage:
			saveBooleanSetting(getString(R.string.setting_pre_delete_message),
					chkDeleteMessage.isChecked());
			break;
		case R.id.chkEnableFilterByPhone:
			saveBooleanSetting(
					getString(R.string.setting_pre_enable_phone_filter),
					chkEnablePhoneFilter.isChecked());
			break;
		case R.id.chkEnableFilterByContent:
			saveBooleanSetting(
					getString(R.string.setting_pre_enable_content_filter),
					chkEnableContentFilter.isChecked());
			break;
		default:
			break;
		}
	}

	private void saveBooleanSetting(String key, boolean value) {
		SharedPreferences sharedPref = this.getSharedPreferences(
				MESSAGE_SETTING_ATTRIBUTE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	private void loadSettings() {
		// Load settings preferences
		SharedPreferences sharedPref = this.getSharedPreferences(
				MESSAGE_SETTING_ATTRIBUTE, Context.MODE_PRIVATE);
		boolean allowDeleteMessage = sharedPref.getBoolean(
				getString(R.string.setting_pre_delete_message), false);
		boolean enablePhoneFilter = sharedPref.getBoolean(
				getString(R.string.setting_pre_enable_phone_filter), false);
		boolean enableContentFilter = sharedPref.getBoolean(
				getString(R.string.setting_pre_enable_content_filter), false);
		// Set setting preferences
		chkDeleteMessage.setChecked(allowDeleteMessage);
		chkEnablePhoneFilter.setChecked(enablePhoneFilter);
		chkEnableContentFilter.setChecked(enableContentFilter);
	}
}
