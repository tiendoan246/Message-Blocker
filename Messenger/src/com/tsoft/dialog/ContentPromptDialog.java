package com.tsoft.dialog;

import java.util.Hashtable;
import java.util.Set;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tsoft.callback.CustomDialogListener;
import com.tsoft.content.MessageFilterContent;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.messenger.R;
import com.tsoft.util.DatabaseManagerUtil;

public class ContentPromptDialog extends Dialog implements
		android.view.View.OnClickListener {
	private Button btnSave, btnCancel;
	private EditText etInput;
	private RadioButton rdbAuto, rdbManual;
	private Spinner spPattern;
	private TextView tvSelectYourPattern, tvContentManualTitle;
	private Context context;

	private ArrayAdapter<CharSequence> adapter;
	private Hashtable<String, String> patterns;

	// Dialog listener
	private CustomDialogListener dialogListener;

	public CustomDialogListener getDialogListener() {
		return dialogListener;
	}

	public void setDialogListener(CustomDialogListener dialogListener) {
		this.dialogListener = dialogListener;
	}

	// List phone filter
	private Set<String> filterList;

	public ContentPromptDialog(Context context, int theme, Set<String> filter) {
		super(context, theme);
		this.context = context;
		this.filterList = filter;
		initPatternsList(context);
	}

	private void initPatternsList(Context context) {
		patterns = new Hashtable<String, String>();
		patterns.put(
				this.context.getString(R.string.setting_filters_auto_phone),
				context.getString(R.string.setting_pre_phone_regex));
		patterns.put(
				this.context.getString(R.string.setting_filters_auto_email),
				context.getString(R.string.setting_pre_email_regex));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set dialog style
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.content_prompt_layout);
		setCanceledOnTouchOutside(false);

		btnSave = (Button) findViewById(R.id.btnContentSave);
		btnCancel = (Button) findViewById(R.id.btnContentCancel);
		etInput = (EditText) findViewById(R.id.etContentInput);
		rdbAuto = (RadioButton) findViewById(R.id.rdbFilterAuto);
		rdbManual = (RadioButton) findViewById(R.id.rdbFilterManual);
		tvSelectYourPattern = (TextView) findViewById(R.id.tvSelectYourPattern);
		spPattern = (Spinner) findViewById(R.id.spFilterType);
		tvContentManualTitle = (TextView) findViewById(R.id.tvContentManualTitle);

		// Set default radio state
		rdbAuto.setChecked(true);
		setControlVisible(true);

		// Set adapter
		adapter = ArrayAdapter.createFromResource(context, R.array.filter_type,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spPattern.setAdapter(adapter);

		// Set on click listener
		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		rdbAuto.setOnClickListener(this);
		rdbManual.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Toast toast = null;

		switch (v.getId()) {
		case R.id.btnContentSave:
			if (rdbAuto.isChecked()) {
				String selectPatternKey = spPattern.getSelectedItem()
						.toString();
				String pattern = patterns.get(selectPatternKey);
				if (pattern == null || pattern.length() == 0) {
					toast = Toast
							.makeText(
									context,
									context.getString(R.string.message_toast_content_auto_required),
									Toast.LENGTH_SHORT);
					toast.show();
				} else {
					if (filterList.contains(pattern)) {
						toast = Toast
								.makeText(
										context,
										context.getString(R.string.message_toast_content_existed),
										Toast.LENGTH_SHORT);
						toast.show();
					} else {
						this.filterList.add(pattern);
						FilterSettingModel setting = new FilterSettingModel();
						setting.setType(MessageFilterContent.SETTING_TYPE_CONTENT);
						setting.setValue(pattern);

						// Insert
						DatabaseManagerUtil.getInstance(this.context)
								.insertSetting(setting);

						// Return the value
						this.dialogListener.userSelectedValue(pattern);
						this.dismiss();
					}
				}
			} else {
				String result = etInput.getText().toString();
				if (result == null || result.length() == 0) {
					toast = Toast
							.makeText(
									context,
									context.getString(R.string.message_toast_content_manual_required),
									Toast.LENGTH_SHORT);
					toast.show();
				} else {
					if (filterList.contains(result)) {
						toast = Toast
								.makeText(
										context,
										context.getString(R.string.message_toast_content_existed),
										Toast.LENGTH_SHORT);
						toast.show();
					} else {
						this.filterList.add(result);
						FilterSettingModel setting = new FilterSettingModel();
						setting.setType(MessageFilterContent.SETTING_TYPE_CONTENT);
						setting.setValue(result);

						// Insert
						DatabaseManagerUtil.getInstance(this.context)
								.insertSetting(setting);

						// Return the value
						this.dialogListener.userSelectedValue(result);
						this.dismiss();
					}
				}
			}
			break;
		case R.id.rdbFilterAuto:
			setControlVisible(true);
			break;
		case R.id.rdbFilterManual:
			setControlVisible(false);
			break;
		case R.id.btnContentCancel:
			this.cancel();
			break;
		default:
			break;
		}
	}

	private void setControlVisible(boolean isAuto) {
		if (isAuto) {
			tvSelectYourPattern.setVisibility(TextView.VISIBLE);
			spPattern.setVisibility(Spinner.VISIBLE);
			tvContentManualTitle.setVisibility(TextView.GONE);
			etInput.setVisibility(EditText.GONE);
		} else {
			tvSelectYourPattern.setVisibility(TextView.GONE);
			spPattern.setVisibility(Spinner.GONE);
			tvContentManualTitle.setVisibility(TextView.VISIBLE);
			etInput.setVisibility(EditText.VISIBLE);
		}
	}

	public Set<String> getFilterList() {
		return filterList;
	}

	public void setFilterList(Set<String> filterList) {
		this.filterList = filterList;
	}
}
