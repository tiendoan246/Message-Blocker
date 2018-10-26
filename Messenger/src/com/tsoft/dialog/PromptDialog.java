package com.tsoft.dialog;

import java.util.Set;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tsoft.callback.CustomDialogListener;
import com.tsoft.content.MessageFilterContent;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.messenger.R;
import com.tsoft.util.DatabaseManagerUtil;

public class PromptDialog extends Dialog implements
		android.view.View.OnClickListener {

	private Button btnOk, btnCancel;
	private EditText etInput;
	private Context context;

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

	public PromptDialog(Context context, int theme, Set<String> filter) {
		super(context, theme);
		this.context = context;
		this.filterList = filter;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set dialog style
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.prompt_layout);
		setCanceledOnTouchOutside(false);

		btnOk = (Button) findViewById(R.id.btnInputOK);
		btnCancel = (Button) findViewById(R.id.btnInputCancel);
		etInput = (EditText) findViewById(R.id.etUserInput);

		// Set on click listener
		btnOk.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Toast toast = null;

		switch (v.getId()) {
		case R.id.btnInputOK:
			String result = etInput.getText().toString();
			if (result == null || result.length() == 0) {
				toast = Toast
						.makeText(
								context,
								context.getString(R.string.message_toast_require_input),
								Toast.LENGTH_SHORT);
				toast.show();
			} else {
				if (filterList.contains(result)) {
					toast = Toast.makeText(context, context
							.getString(R.string.message_toast_phone_existed),
							Toast.LENGTH_SHORT);
					toast.show();
				} else {
					this.filterList.add(result);
					FilterSettingModel setting = new FilterSettingModel();
					setting.setType(MessageFilterContent.SETTING_TYPE_PHONE);
					setting.setValue(result);

					// Insert
					DatabaseManagerUtil.getInstance(this.context)
							.insertSetting(setting);

					// Return the value
					this.dialogListener.userSelectedValue(result);
					this.dismiss();
				}
			}
			break;
		case R.id.btnInputCancel:
			this.cancel();
			break;
		default:
			break;
		}
	}

	public Set<String> getFilterList() {
		return filterList;
	}

	public void setFilterList(Set<String> filterList) {
		this.filterList = filterList;
	}
}
