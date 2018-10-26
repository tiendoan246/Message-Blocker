package com.tsoft.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tsoft.datamodel.PhoneModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.messenger.R;
import com.tsoft.util.ColorGenerator;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.PhoneManagerUtil;
import com.tsoft.util.TextDrawable;

public class CustomListAdapter extends BaseAdapter {
	private static final int MAX_MESSAGE_LENGTH = 80;
	private Context context;
	private LayoutInflater inflater;
	private List<SmsModel> smsItems;
	private String _search = "";

	// declare the color generator and drawable builder
	private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;

	public CustomListAdapter(Context context, boolean showAll) {
		this.context = context;
		this.smsItems = DatabaseManagerUtil.getInstance(context)
				.getSpamMessages();
	}

	public CustomListAdapter(Context context, List<SmsModel> smsItems) {
		this.context = context;
		this.smsItems = smsItems;
	}

	public void search(String search){
		this._search = search;
		this.smsItems = DatabaseManagerUtil.getInstance(context)
				.getSpamMessages(search);
	}
	
	@Override
	public int getCount() {
		return smsItems.size();
	}

	@Override
	public Object getItem(int location) {
		return smsItems.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void removeItem(int location) {
		smsItems.remove(getItem(location));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.list_row, null);

		ImageView thumbNail = (ImageView) convertView
				.findViewById(R.id.ivThumbnail);
		TextView tvphone = (TextView) convertView.findViewById(R.id.tvPhone);
		TextView tvbody = (TextView) convertView.findViewById(R.id.tvBody);
		TextView tvdate = (TextView) convertView.findViewById(R.id.tvDate);

		// getting movie data for the row
		SmsModel m = smsItems.get(position);

		TextDrawable drawable = null;

		// phone
		// Replace VN region
		String phone = m.getPhone();

		PhoneModel phoneModel = PhoneManagerUtil.getIntance(this.context)
				.getPhoneByNumber(phone);
		tvphone.setText(phoneModel != null ? phoneModel.getDisplayName() : m
				.getPhone());

		// The contact is exist
		if (phoneModel != null) {
			drawable = TextDrawable.builder().buildRound(
					String.valueOf(phoneModel.getDisplayName().charAt(0)),
					mColorGenerator.getColor(phoneModel.getDisplayName()));
		} else {
			drawable = TextDrawable.builder().buildRound(
					ContextCompat.getDrawable(context,
							R.drawable.contact_center),
					mColorGenerator.getColor(phone));
		}

		// thumbnail image
		thumbNail.setImageDrawable(drawable);

		// body
		String message = m.getMessage();
		if (message.length() > MAX_MESSAGE_LENGTH) {
			message = message.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
		}
		tvbody.setText(message);

		// date
		// Date dateValue = new Date(m.getDate());
		// SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		// tvdate.setText(df.format(dateValue));

		return convertView;
	}

}
