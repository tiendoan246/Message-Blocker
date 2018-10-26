package com.tsoft.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tsoft.datamodel.SmsModel;
import com.tsoft.messenger.R;
import com.tsoft.util.ConstantUtil;
import com.tsoft.util.DateUtility;
import com.tsoft.util.MessageConst;
import com.tsoft.util.SmsMessageManager;

public class MessagesListAdapter extends BaseAdapter {

	private Context context;
	private List<SmsModel> messagesItems;

	public MessagesListAdapter(Context context, List<SmsModel> navDrawerItems) {
		this.context = context;
		this.messagesItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		return messagesItems.size();
	}

	@Override
	public SmsModel getItem(int position) {
		return messagesItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void removeItem(int location) {
		messagesItems.remove(getItem(location));
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SmsModel m = messagesItems.get(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (messagesItems.get(position).isSender()) {
			convertView = mInflater.inflate(R.layout.list_item_message_right,
					null);
		} else {
			convertView = mInflater.inflate(R.layout.list_item_message_left,
					null);
		}

		TextView lblFrom = (TextView) convertView.findViewById(R.id.lblMsgFrom);
		TextView txtMsg = (TextView) convertView.findViewById(R.id.txtMsg);
		txtMsg.setText(m.getMessage());

		// Date
		if (m.getStatus() == SmsMessageManager.STATUS_COMPLETE) {
			lblFrom.setText(DateUtility.getInstance(context).getDateTime(
					m.getDate()));
		} else {
			lblFrom.setText(MessageConst.getIntance(this.context)
					.getMessageStatus(m.getStatus()));
		}

		return convertView;
	}

	public void updateData(SmsModel sms, int status) {
		SmsModel item = messagesItems.get(messagesItems.size() - 1);
		switch (status) {
		case SmsMessageManager.STATUS_PENDING:
			item.setSendStatus(this.context
					.getString(R.string.compose_message_sending) + "...");
			break;
		case SmsMessageManager.STATUS_FAILED:
			item.setSendStatus(this.context
					.getString(R.string.compose_message_failed));
			break;
		case ConstantUtil.SMS_SENT:
			item.setSendStatus(this.context
					.getString(R.string.compose_message_sent));
			break;
		}
		notifyDataSetChanged();
	}
}
