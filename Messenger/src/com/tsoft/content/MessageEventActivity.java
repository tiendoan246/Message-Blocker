package com.tsoft.content;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.faizmalkani.floatingactionbutton.FloatingActionButton;
import com.tsoft.adapter.CustomScheduleMessageAdapter;
import com.tsoft.datamodel.MessageScheduleModel;
import com.tsoft.messenger.CreateScheduleMessageActivity;
import com.tsoft.messenger.R;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.ScheduleTaskManagerUtil;

public class MessageEventActivity extends Fragment implements OnClickListener {

	private static final String TAG = "MessageEventActivity";

	public static final int ADD_MESSAGE_SCHEDULE = 22;

	// Context menu
	public static final int CONTEXT_ITEM_DELETE = 1;

	private ListView listView;
	private FloatingActionButton btnNewMessage;

	private Context context;

	private ArrayAdapter<MessageScheduleModel> adapter;

	public MessageEventActivity(Context context) {
		this.context = context;
	}

	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		Animation anim = null;
		if (enter) {
			anim = AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.slide_in_left);
		} else {
			anim = AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.slide_out_right);
			anim.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {

				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {

				}
			});
		}

		// NOTE: the animation must be added to an animation set in order for
		// the listener
		// to work on the exit animation
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(anim);

		return animSet;
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.activity_message_event, container,
				false);

		try {
			// Set controls
			listView = (ListView) v.findViewById(R.id.lvMessageEvents);
			btnNewMessage = (FloatingActionButton) v
					.findViewById(R.id.btnMessagesEventAdd);

			// Set listener
			btnNewMessage.setOnClickListener(this);

			// Register context menu
			this.registerForContextMenu(listView);

			// Load schedule events
			List<MessageScheduleModel> schedules = DatabaseManagerUtil
					.getInstance(this.context).getMessageSchedules();

			adapter = new CustomScheduleMessageAdapter(this.context, schedules);
			// Set adapter
			listView.setAdapter(adapter);
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}

		return v;
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btnMessagesEventAdd:
			intent = new Intent(this.context,
					CreateScheduleMessageActivity.class);
			startActivityForResult(intent, ADD_MESSAGE_SCHEDULE);
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == ADD_MESSAGE_SCHEDULE
				&& resultCode == Activity.RESULT_OK) {
			long id = data.getLongExtra(
					CreateScheduleMessageActivity.MESSAGE_SCHEDULE_ID, -1);
			if (id != -1) {
				MessageScheduleModel message = DatabaseManagerUtil.getInstance(
						this.context).getMessageSchedule(id);
				if (message != null) {
					adapter.add(message);
					adapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
		if (v.getId() == R.id.lvMessageEvents) {
			menu.add(Menu.NONE, CONTEXT_ITEM_DELETE, 0,
					this.getString(R.string.context_item_delete));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Get extra info about list item that was long-pressed
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();

		// Perform action according to selected item from context menu
		switch (item.getItemId()) {

		case CONTEXT_ITEM_DELETE:
			try {
				// Process for conversation items
				MessageScheduleModel message = (MessageScheduleModel) adapter
						.getItem(menuInfo.position);
				DatabaseManagerUtil.getInstance(this.context).delete(
						message.getId());
				adapter.remove(message);
				adapter.notifyDataSetChanged();

				// Cancel schedule
				ScheduleTaskManagerUtil.getInstance(this.context).cancel(
						message.getId());
			} catch (Exception ex) {
			}
			break;
		}
		return true;
	}
}
