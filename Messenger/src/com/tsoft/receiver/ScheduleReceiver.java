package com.tsoft.receiver;

import com.tsoft.service.MessageService;
import com.tsoft.util.ScheduleTaskManagerUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, MessageService.class);
		context.startService(serviceIntent);

		// Start the new queue schedule when boot completed
		ScheduleTaskManagerUtil.getInstance(context).createSchedule();
	}

}
