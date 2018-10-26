package com.tsoft.messenger;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.tsoft.adapter.PhoneAutocompleteAdapter;
import com.tsoft.control.ContactsCompletionView;
import com.tsoft.control.TokenCompleteTextView.TokenClickStyle;
import com.tsoft.control.TokenCompleteTextView.TokenListener;
import com.tsoft.datamodel.MessageScheduleModel;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.DateUtility;
import com.tsoft.util.PhoneManagerUtil;
import com.tsoft.util.ScheduleTaskManagerUtil;

public class CreateScheduleMessageActivity extends FragmentActivity implements
		OnClickListener, TokenListener<PhoneModel> {
	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	public static final String MESSAGE_SCHEDULE_ID = "message_schedule_id";

	private Button btnSetDate, btnSetTime, btnCancel, btnSave;
	private EditText etMessage;
	private ContactsCompletionView autoCompletedPhones;

	private PhoneAutocompleteAdapter mAdapter;
	private Map<String, String> destinationPhone = null;
	private Calendar sendOnDateValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_schedule_message);

		try {
			setFinishOnTouchOutside(false);

			// Get control
			btnSetDate = (Button) findViewById(R.id.btnScheduleSetDate);
			btnSetTime = (Button) findViewById(R.id.btnScheduleSetTime);
			btnCancel = (Button) findViewById(R.id.btnScheduleCancel);
			btnSave = (Button) findViewById(R.id.btnScheduleSave);
			etMessage = (EditText) findViewById(R.id.etMessageScheduleContent);
			autoCompletedPhones = (ContactsCompletionView) findViewById(R.id.etScheduleSearchContact);

			destinationPhone = new HashMap<String, String>();
			sendOnDateValue = Calendar.getInstance();

			// load phone
			try {
				if (PhoneManagerUtil.getIntance(this).isPhonesEmpty()) {
					PhoneManagerUtil.getIntance(this).cacheAllThreads();
				}
			} catch (Exception ex) {
				Log.e(TAG, ex.getMessage());
			}

			// Set values
			btnSetDate.setText(DateUtility.getInstance(this).getDateName(this,
					new Date()));
			btnSetTime.setText(DateUtility.getInstance(this).getTimeName(this,
					new Date()));

			// Set listener
			btnSetDate.setOnClickListener(this);
			btnSetTime.setOnClickListener(this);
			btnCancel.setOnClickListener(this);
			btnSave.setOnClickListener(this);
			autoCompletedPhones.setTokenListener(this);
			autoCompletedPhones.setTokenClickStyle(TokenClickStyle.Delete);

			// Adapter
			mAdapter = new PhoneAutocompleteAdapter(this);
			autoCompletedPhones.setAdapter(mAdapter);
			autoCompletedPhones.allowDuplicates(false);
			autoCompletedPhones.allowCollapse(true);
			autoCompletedPhones
					.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	@Override
	public void onClick(View v) {
		Toast toast = null;
		switch (v.getId()) {
		case R.id.btnScheduleSetDate:
			showDatePickerDialog(v);
			break;
		case R.id.btnScheduleSetTime:
			showTimePickerDialog(v);
			break;
		case R.id.btnScheduleCancel:
			this.finish();
			break;
		case R.id.btnScheduleSave:
			// Check recipients
			String recipients = autoCompletedPhones.getText().toString();
			if (recipients == null || recipients.length() == 0) {
				toast = Toast
						.makeText(
								this,
								this.getString(R.string.compose_message_send_recipents_required),
								Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			// Check message contents
			String msg = etMessage.getText().toString();
			if (msg == null || msg.length() == 0) {
				toast = Toast.makeText(this,
						this.getString(R.string.compose_message_send_required),
						Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			// Check send on date
			if (sendOnDateValue.getTimeInMillis() <= new Date().getTime()) {
				toast = Toast
						.makeText(
								this,
								this.getString(R.string.message_schedule_send_on_date_less_than_current),
								Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			long id = saveMessageSchedule();

			// Start schedule
			ScheduleTaskManagerUtil.getInstance(this).createSchedule();

			Intent returnIntent = new Intent();
			returnIntent.putExtra(MESSAGE_SCHEDULE_ID, id);
			setResult(RESULT_OK, returnIntent);
			this.finish();
			break;
		}
	}

	private long saveMessageSchedule() {
		MessageScheduleModel message = new MessageScheduleModel();
		message.setCreatedDate(new Date().getTime());
		message.setDestinationAddress(getDestinationPhones());
		message.setMessage(etMessage.getText().toString());
		message.setSentOnDate(sendOnDateValue.getTimeInMillis());
		message.setSourceAddress("");

		return DatabaseManagerUtil.getInstance(this).insert(message);
	}

	private String getDestinationPhones() {
		StringBuilder builder = new StringBuilder();
		Set<String> keys = destinationPhone.keySet();
		for (String key : keys) {
			builder.append(destinationPhone.get(key));
			builder.append(";");
		}
		return builder.toString();
	}

	private void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	public void showTimePickerDialog(View v) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), "timePicker");
	}

	public class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			sendOnDateValue.set(Calendar.HOUR_OF_DAY, hourOfDay);
			sendOnDateValue.set(Calendar.MINUTE, minute);
			// Set display time
			btnSetTime.setText(DateUtility.getInstance(
					CreateScheduleMessageActivity.this).getTimeName(
					this.getActivity(), sendOnDateValue.getTime()));
		}
	}

	public class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			sendOnDateValue.set(Calendar.YEAR, year);
			sendOnDateValue.set(Calendar.MONTH, month);
			sendOnDateValue.set(Calendar.DAY_OF_MONTH, day);
			// Set display time
			btnSetDate.setText(DateUtility.getInstance(
					CreateScheduleMessageActivity.this).getDateName(
					this.getActivity(), sendOnDateValue.getTime()));
		}
	}

	@Override
	public void onTokenAdded(PhoneModel arg0) {
		if (!destinationPhone.containsKey(arg0.getDisplayName())) {
			destinationPhone.put(arg0.getDisplayName(), arg0.getNumber());
		}
	}

	@Override
	public void onTokenRemoved(PhoneModel arg0) {
		if (destinationPhone.containsKey(arg0.getDisplayName())) {
			destinationPhone.remove(arg0.getDisplayName());
		}
	}

}
