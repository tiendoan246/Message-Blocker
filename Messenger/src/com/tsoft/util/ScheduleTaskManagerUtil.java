package com.tsoft.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tsoft.datamodel.MessageScheduleModel;
import com.tsoft.receiver.AlarmScheduleReceiver;

public class ScheduleTaskManagerUtil {

	private static ScheduleTaskManagerUtil _instance;
	private Context _context;

	private static Map<Long, PendingIntent> alarmSchedule = new HashMap<Long, PendingIntent>();

	private ScheduleTaskManagerUtil(Context context) {
		this._context = context;
	}

	public static ScheduleTaskManagerUtil getInstance(Context context) {
		if (_instance == null) {
			_instance = new ScheduleTaskManagerUtil(context);
		}
		return _instance;
	}

	public void createSchedule() {
		// Get first schedule message that have close date with the current
		MessageScheduleModel message = DatabaseManagerUtil
				.getInstance(_context).getFirstMessageScheduleSending();

		if (message != null) {
			// Is the schedule is order than one day, remove it
			Date current = new Date();
			// create a date one day before current date
			long prevDay = message.getSentOnDate() + 1000 * 60 * 60 * 24;
			// create date object
			Date prev = new Date(prevDay);

			// Calendar sendOnDate = Calendar.getInstance();
			// sendOnDate.setTimeInMillis(message.getSentOnDate());

			if (prev.before(current)) {
				DatabaseManagerUtil.getInstance(_context).delete(
						message.getId());
				// Create other schedule and delete older schedule
				createSchedule();
				return;
			}

			// Create a intent for processing schedule
			Intent intent = new Intent(_context, AlarmScheduleReceiver.class);
			// intent.setAction(Long.toString(System.currentTimeMillis()));

			PendingIntent pendingIntent = PendingIntent.getBroadcast(_context,
					0, intent, 0);

			AlarmManager alarm = (AlarmManager) _context
					.getSystemService(Context.ALARM_SERVICE);

			// schedule for every 30 (30 * 1000) seconds
			alarm.setRepeating(AlarmManager.RTC_WAKEUP,
					message.getSentOnDate(), 3000 * 1000, pendingIntent);
			// Return alarm manager
			alarmSchedule.put(message.getId(), pendingIntent);
		}
	}

	public void cancel(long scheduleId) {
		PendingIntent pendingIntent = null;
		if (alarmSchedule.containsKey(scheduleId)) {
			pendingIntent = alarmSchedule.get(scheduleId);
		}
		if (pendingIntent != null) {
			AlarmManager manager = (AlarmManager) _context
					.getSystemService(Context.ALARM_SERVICE);
			manager.cancel(pendingIntent);

			// Remove pending intent
			alarmSchedule.remove(scheduleId);
		}
	}
}
