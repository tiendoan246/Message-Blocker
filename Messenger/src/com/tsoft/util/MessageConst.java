package com.tsoft.util;

import java.util.HashMap;
import java.util.Map;

import com.tsoft.messenger.R;

import android.content.Context;

public class MessageConst {

	private static MessageConst _intance;

	private Context context;
	private Map<Integer, String> messageStatus;

	private MessageConst(Context context) {
		messageStatus = new HashMap<Integer, String>();
		this.context = context;

		// Initial messages status
		initMessageStatus();
	}

	public static MessageConst getIntance(Context context) {
		if (_intance == null) {
			_intance = new MessageConst(context);
		}
		return _intance;
	}

	private void initMessageStatus() {
		messageStatus.put(SmsMessageManager.STATUS_FAILED,
				this.context.getString(R.string.compose_message_status_falied));
		messageStatus.put(SmsMessageManager.STATUS_COMPLETE, this.context
				.getString(R.string.compose_message_status_completed));
		messageStatus
				.put(SmsMessageManager.STATUS_PENDING, this.context
						.getString(R.string.compose_message_status_sending));
		messageStatus.put(ConstantUtil.SMS_SENT,
				this.context.getString(R.string.compose_message_sent));
	}

	public String getMessageStatus(int status) {
		return messageStatus.get(status);
	}

}
