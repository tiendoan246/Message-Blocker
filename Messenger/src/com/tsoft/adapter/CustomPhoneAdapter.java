package com.tsoft.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tsoft.callback.CheckboxStateChangeListener;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.messenger.R;

public class CustomPhoneAdapter extends ArrayAdapter<PhoneModel> {
	private LayoutInflater inflater;
	private CheckboxStateChangeListener<PhoneModel> stateChangedListener;

	public CustomPhoneAdapter(Context context, List<PhoneModel> phoneItems,
			CheckboxStateChangeListener<PhoneModel> stateChangedListener) {
		super(context, R.layout.list_row_contact, R.id.tvContactName,
				phoneItems);
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater = LayoutInflater.from(context);
		this.stateChangedListener = stateChangedListener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Phone to display
		PhoneModel phone = (PhoneModel) this.getItem(position);

		// The child views in each row.
		CheckBox chkCheckBox;
		TextView tvContactName, tvContactNumber;

		// Create a new row view
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_row_contact, null);

			// Find the child views.
			tvContactName = (TextView) convertView
					.findViewById(R.id.tvContactName);
			tvContactNumber = (TextView) convertView
					.findViewById(R.id.tvContactNumber);
			chkCheckBox = (CheckBox) convertView.findViewById(R.id.chkContact);

			// Optimization: Tag the row with it's child views, so we don't have
			// to
			// call findViewById() later when we reuse the row.
			convertView.setTag(new PhoneViewHolder(tvContactName,
					tvContactNumber, chkCheckBox));

			// If CheckBox is toggled, update the planet it is tagged with.
			chkCheckBox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					PhoneModel _phone = (PhoneModel) cb.getTag();
					_phone.setChecked(cb.isChecked());

					// Raise on state changed
					if (stateChangedListener != null) {
						stateChangedListener.OnCheckedBoxStateChanged(_phone);
					}
				}
			});
		}
		// Reuse existing row view
		else {
			// Because we use a ViewHolder, we avoid having to call
			// findViewById().
			PhoneViewHolder viewHolder = (PhoneViewHolder) convertView.getTag();
			chkCheckBox = viewHolder.getChkCheckbox();
			tvContactName = viewHolder.getTvContactName();
			tvContactNumber = viewHolder.getTvContactNumber();
		}

		// Tag the CheckBox with the Planet it is displaying, so that we can
		// access the planet in onClick() when the CheckBox is toggled.
		chkCheckBox.setTag(phone);

		// Display planet data
		chkCheckBox.setChecked(phone.isChecked());
		tvContactName.setText(phone.getDisplayName());
		tvContactNumber.setText(phone.getNumber());

		return convertView;

	}

	/** Holds child views for one row. */
	public static class PhoneViewHolder {
		private CheckBox chkCheckbox;
		private TextView tvContactName, tvContactNumber;

		public CheckBox getChkCheckbox() {
			return chkCheckbox;
		}

		public void setChkCheckbox(CheckBox chkCheckbox) {
			this.chkCheckbox = chkCheckbox;
		}

		public TextView getTvContactName() {
			return tvContactName;
		}

		public void setTvContactName(TextView tvContactName) {
			this.tvContactName = tvContactName;
		}

		public TextView getTvContactNumber() {
			return tvContactNumber;
		}

		public void setTvContactNumber(TextView tvContactNumber) {
			this.tvContactNumber = tvContactNumber;
		}

		public PhoneViewHolder() {
		}

		public PhoneViewHolder(TextView tvContactName,
				TextView tvContactNumber, CheckBox chkCheckbox) {
			this.chkCheckbox = chkCheckbox;
			this.tvContactName = tvContactName;
			this.tvContactNumber = tvContactNumber;
		}
	}
}
