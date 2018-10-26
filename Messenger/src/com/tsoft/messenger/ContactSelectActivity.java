package com.tsoft.messenger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tsoft.adapter.CustomPhoneAdapter;
import com.tsoft.adapter.CustomPhoneAdapter.PhoneViewHolder;
import com.tsoft.callback.CheckboxStateChangeListener;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.util.PhoneManagerUtil;

public class ContactSelectActivity extends Activity implements OnClickListener {

	private static final String TAG = "ContactSelectActivity";

	private ListView listView;
	private EditText etSearchContact;
	private Button btnCancel, btnOk;
	private LinearLayout bottomLayout;
	private ArrayAdapter<PhoneModel> adapter;
	private List<PhoneModel> _phones;
	private List<PhoneModel> _temp;

	public static final String CONTACT_RESULT = "contact_result";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_select);

		try {
			// Set controls
			etSearchContact = (EditText) findViewById(R.id.etSearchContact);
			listView = (ListView) findViewById(R.id.lvContactList);
			bottomLayout = (LinearLayout) findViewById(R.id.layoutBottom);
			btnCancel = (Button) findViewById(R.id.btnContactCancel);
			btnOk = (Button) findViewById(R.id.btnContactOk);

			// If phone list is empty, load it
			if (PhoneManagerUtil.getIntance(this).isPhonesEmpty()) {
				PhoneManagerUtil.getIntance(this).cacheAllThreads();
			}

			CheckboxStateChangeListener<PhoneModel> stateChangedListener = new CheckboxStateChangeListener<PhoneModel>() {

				@Override
				public void OnCheckedBoxStateChanged(PhoneModel item) {
					// Check checked states
					boolean isChecked = checkCheckedStates(_phones);
					if (isChecked) {
						bottomLayout.setVisibility(LinearLayout.VISIBLE);
					} else {
						bottomLayout.setVisibility(LinearLayout.GONE);
					}
				}
			};

			// Get phone list
			_phones = PhoneManagerUtil.getIntance(this).getPhones();
			_temp = new ArrayList<PhoneModel>(_phones);

			// Set checked state
			PhoneManagerUtil.getIntance(this).setCheckedStates(false);
			adapter = new CustomPhoneAdapter(this, _temp, stateChangedListener);

			// Set adapter
			listView.setAdapter(adapter);

			// Register listener
			btnCancel.setOnClickListener(this);
			btnOk.setOnClickListener(this);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View item,
						int position, long id) {
					PhoneModel phone = adapter.getItem(position);
					phone.toggleChecked();
					PhoneViewHolder viewHolder = (PhoneViewHolder) item
							.getTag();
					viewHolder.getChkCheckbox().setChecked(phone.isChecked());

					// Check checked states
					boolean isChecked = checkCheckedStates(_phones);
					if (isChecked) {
						bottomLayout.setVisibility(LinearLayout.VISIBLE);
					} else {
						bottomLayout.setVisibility(LinearLayout.GONE);
					}
				}
			});
			etSearchContact.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					_phones = PhoneManagerUtil.getIntance(
							ContactSelectActivity.this).searchPhones(
							s.toString().toUpperCase(Locale.getDefault()));
					_temp = new ArrayList<PhoneModel>(_phones);
					adapter.clear();
					adapter.addAll(_temp);
					adapter.notifyDataSetChanged();
				}
			});
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	private boolean checkCheckedStates(List<PhoneModel> phones) {
		for (PhoneModel phone : phones) {
			if (phone.isChecked()) {
				return true;
			}
		}
		return false;
	}

	private ArrayList<PhoneModel> getCheckedPhones(List<PhoneModel> phones) {
		ArrayList<PhoneModel> results = new ArrayList<PhoneModel>();
		for (PhoneModel phone : phones) {
			if (phone.isChecked()) {
				results.add(phone);
			}
		}
		return results;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnContactOk:
			ArrayList<PhoneModel> checkedPhones = getCheckedPhones(_phones);
			Intent returnIntent = new Intent();
			returnIntent.putParcelableArrayListExtra(CONTACT_RESULT,
					checkedPhones);
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		case R.id.btnContactCancel:
			this.finish();
			break;
		default:
			break;
		}
	}
}
