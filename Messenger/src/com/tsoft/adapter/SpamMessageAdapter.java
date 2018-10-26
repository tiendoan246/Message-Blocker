package com.tsoft.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
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

public class SpamMessageAdapter extends BaseAdapter {
	private static final int MAX_MESSAGE_LENGTH = 80;
	private static final int HIGHLIGHT_COLOR = 0x999be6ff;
	private Context context;
	private LayoutInflater inflater;
	private List<SmsModel> smsItems;

	// declare the color generator and drawable builder
	private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;

	public SpamMessageAdapter(Context context) {
		this.context = context;
		this.smsItems = DatabaseManagerUtil.getInstance(context)
				.getSpamMessages();
	}

	public SpamMessageAdapter(Context context, List<SmsModel> smsItems) {
		this.context = context;
		this.smsItems = smsItems;
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
	public View getView(final int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_row_spam, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// getting movie data for the row
		SmsModel m = smsItems.get(position);

		// phone
		// Replace VN region
		String phone = m.getPhone();

		PhoneModel phoneModel = PhoneManagerUtil.getIntance(this.context)
				.getPhoneByNumber(phone);
		holder.tvphone.setText(phoneModel != null ? phoneModel.getDisplayName()
				: m.getPhone());

		// The contact is exist
		final String str = phoneModel != null ? phoneModel.getDisplayName()
				: phone;

		// provide support for selected state
		updateCheckedState(holder, m, str);
		holder.thumbNail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// when the image is clicked, update the selected state
				SmsModel data = smsItems.get(position);
				data.setSelected(!data.isSelected());
				updateCheckedState(holder, data, str);
			}
		});

		// body
		String message = m.getMessage();
		if (message.length() > MAX_MESSAGE_LENGTH) {
			message = message.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
		}
		holder.tvbody.setText(message);

		return convertView;
	}

	private void updateCheckedState(ViewHolder holder, SmsModel item, String str) {
		if (item.isSelected()) {
			holder.thumbNail.setImageDrawable(TextDrawable.builder()
					.buildRound(" ", 0xff616161));
			holder.view.setBackgroundColor(HIGHLIGHT_COLOR);
			holder.checkIcon.setVisibility(View.VISIBLE);
		} else {
			TextDrawable drawable = TextDrawable.builder().buildRound(
					String.valueOf(str.charAt(0)),
					mColorGenerator.getColor(str));
			holder.thumbNail.setImageDrawable(drawable);
			holder.view.setBackgroundColor(Color.TRANSPARENT);
			holder.checkIcon.setVisibility(View.GONE);
		}
	}

	private static class ViewHolder {

		private View view;

		private ImageView thumbNail;
		private TextView tvphone;
		private TextView tvbody;
		private ImageView checkIcon;

		private ViewHolder(View view) {
			this.view = view;
			checkIcon = (ImageView) view.findViewById(R.id.check_icon);
			thumbNail = (ImageView) view.findViewById(R.id.ivThumbnail);
			checkIcon = (ImageView) view.findViewById(R.id.check_icon);
			tvphone = (TextView) view.findViewById(R.id.tvPhone);
			tvbody = (TextView) view.findViewById(R.id.tvBody);
		}
	}
}