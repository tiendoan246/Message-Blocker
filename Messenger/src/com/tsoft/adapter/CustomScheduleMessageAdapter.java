package com.tsoft.adapter;

import java.text.MessageFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tsoft.datamodel.MessageScheduleModel;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.messenger.R;
import com.tsoft.util.DateUtility;
import com.tsoft.util.PhoneManagerUtil;

public class CustomScheduleMessageAdapter extends
		ArrayAdapter<MessageScheduleModel> {
	private LayoutInflater inflater;
	private Context _context;

	public CustomScheduleMessageAdapter(Context context,
			List<MessageScheduleModel> messageItems) {
		super(context, R.layout.list_row_contact, R.id.tvContactName,
				messageItems);
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater = LayoutInflater.from(context);
		this._context = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Message to display
		MessageScheduleModel message = (MessageScheduleModel) this
				.getItem(position);

		// The child views in each row.
		TextView tvDestinations, tvMessage, tvCreatedDate, tvSendOnDate;

		// Create a new row view
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_row_message_schedule,
					null);

			// Find the child views.
			tvDestinations = (TextView) convertView
					.findViewById(R.id.tvScheduleDestinations);
			tvMessage = (TextView) convertView
					.findViewById(R.id.tvScheduleMessage);
			tvCreatedDate = (TextView) convertView
					.findViewById(R.id.tvScheduleDateCreated);
			tvSendOnDate = (TextView) convertView
					.findViewById(R.id.tvScheduleDateSend);

			// Optimization: Tag the row with it's child views, so we don't have
			// to
			// call findViewById() later when we reuse the row.
			convertView.setTag(new MessageViewHolder(tvDestinations, tvMessage,
					tvCreatedDate, tvSendOnDate));
		}
		// Reuse existing row view
		else {
			// Because we use a ViewHolder, we avoid having to call
			// findViewById().
			MessageViewHolder viewHolder = (MessageViewHolder) convertView
					.getTag();
			tvDestinations = viewHolder.getTvDestinations();
			tvMessage = viewHolder.getTvMessage();
			tvCreatedDate = viewHolder.getTvCreatedDate();
			tvSendOnDate = viewHolder.getTvSendOnDate();
		}

		// Display message data
		tvCreatedDate
				.setText(MessageFormat.format(
						"{0}\n{1}",
						this._context
								.getString(R.string.message_schedule_created_date_row_title),
						DateUtility.getInstance(_context).getDateTime(
								message.getCreatedDate())));
		tvDestinations
				.setText(getDestinations(message.getDestinationAddress()));
		tvMessage.setText(message.getMessage());
		tvSendOnDate
				.setText(MessageFormat.format(
						"{0}\n{1}",
						this._context
								.getString(R.string.message_schedule_send_on_date_row_title),
						DateUtility.getInstance(_context).getDateTime(
								message.getSentOnDate())));

		return convertView;

	}

	private String getDestinations(String destination) {
		StringBuilder builder = new StringBuilder();
		String args[] = destination.split(";");
		for (int i = 0; i < args.length; i++) {
			PhoneModel phoneModel = PhoneManagerUtil.getIntance(_context)
					.getPhoneByNumber(args[i]);
			builder.append(phoneModel.getDisplayName());
			if (i < args.length - 1) {
				builder.append(", ");
			}
			if (i == 5 && i < args.length - 1) {
				builder.append("...");
				break;
			}
		}
		return builder.toString();
	}

	/** Holds child views for one row. */
	public static class MessageViewHolder {
		TextView tvDestinations, tvMessage, tvCreatedDate, tvSendOnDate;

		public MessageViewHolder(TextView tvDestinations, TextView tvMessage,
				TextView tvCreatedDate, TextView tvSendOnDate) {
			this.tvDestinations = tvDestinations;
			this.tvMessage = tvMessage;
			this.tvCreatedDate = tvCreatedDate;
			this.tvSendOnDate = tvSendOnDate;
		}

		public TextView getTvDestinations() {
			return tvDestinations;
		}

		public void setTvDestinations(TextView tvDestinations) {
			this.tvDestinations = tvDestinations;
		}

		public TextView getTvMessage() {
			return tvMessage;
		}

		public void setTvMessage(TextView tvMessage) {
			this.tvMessage = tvMessage;
		}

		public TextView getTvCreatedDate() {
			return tvCreatedDate;
		}

		public void setTvCreatedDate(TextView tvCreatedDate) {
			this.tvCreatedDate = tvCreatedDate;
		}

		public TextView getTvSendOnDate() {
			return tvSendOnDate;
		}

		public void setTvSendDate(TextView tvSendOnDate) {
			this.tvSendOnDate = tvSendOnDate;
		}

	}
}
