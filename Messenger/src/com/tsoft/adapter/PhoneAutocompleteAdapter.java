package com.tsoft.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.tsoft.datamodel.PhoneModel;
import com.tsoft.messenger.R;
import com.tsoft.util.PhoneManagerUtil;

public class PhoneAutocompleteAdapter extends BaseAdapter implements Filterable {

	private Context mContext;
	private List<PhoneModel> resultList = new ArrayList<PhoneModel>();

	public PhoneAutocompleteAdapter(Context context) {
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return resultList.size();
	}

	@Override
	public Object getItem(int position) {
		return resultList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.simple_dropdown_item_2line,
					parent, false);
		}
		PhoneModel phone = (PhoneModel) getItem(position);
		((TextView) convertView.findViewById(R.id.tvAutoCompleteName))
				.setText(phone.getDisplayName());
		((TextView) convertView.findViewById(R.id.tvAutoCompletePhone))
				.setText(phone.getNumber());
		return convertView;
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				if (constraint != null) {
					List<PhoneModel> phones = findPhones(mContext,
							constraint.toString());

					// Assign the data to the FilterResults
					filterResults.values = phones;
					filterResults.count = phones.size();
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				if (results != null && results.count > 0) {
					resultList = (List<PhoneModel>) results.values;
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}

			// @Override
			// public CharSequence convertResultToString(Object resultValue) {
			// PhoneModel phone = (PhoneModel) resultValue;
			// if (phone != null){
			// return phone.getDisplayName();
			// }
			// return null;
			// }
		};
		return filter;
	}

	private List<PhoneModel> findPhones(Context context, String key) {
		return PhoneManagerUtil.getIntance(context).searchPhones(
				key.toUpperCase());
	}
}
