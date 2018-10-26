package com.tsoft.content;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.faizmalkani.floatingactionbutton.FloatingActionButton;
import com.tsoft.adapter.CustomConversationListAdapter;
import com.tsoft.adapter.CustomListAdapter;
import com.tsoft.datamodel.ConversationModel;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.datamodel.SmsModel;
import com.tsoft.messenger.ComposeNewSmsActivity;
import com.tsoft.messenger.ComposeNewSmsKitkatActivity;
import com.tsoft.messenger.ComposeSmsActivity;
import com.tsoft.messenger.ComposeSmsKitkatActivity;
import com.tsoft.messenger.R;
import com.tsoft.util.ConstantUtil;
import com.tsoft.util.DatabaseManagerUtil;
import com.tsoft.util.SmsMessageFilter;
import com.tsoft.util.SmsMessageManager;

public class MessageInbox extends Fragment implements OnClickListener {

	private static final String TAG = "MessagesActivity";

	private ListView listView;
	private BaseAdapter adapter;
	private SmsMessageManager smsManager;
	private FloatingActionButton btnNewMessage;

	private Set<String> phoneFilters;
	private boolean showAll;
	private int index;

	private Context context;

	// Context menu
	public static final int CONTEXT_ITEM_DELETE = 1;
	public static final int CONTEXT_ITEM_BLOCK = 2;
	public static final int CONTEXT_ITEM_UNSPAM = 3;

	// Result activity
	public static final int ACTIVITY_COMPOSE_SMS_RESULT = 10;
	public static final String COMPOSED_RRESULT = "composed_result";
	public static final String COMPOSED_NEW_RESULT = "composed_new_result";

	public MessageInbox(Context context, boolean showAll) {
		this.context = context;
		this.showAll = showAll;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.activity_messages, container, false);

		listView = (ListView) v.findViewById(R.id.lvMessages);
		btnNewMessage = (FloatingActionButton) v
				.findViewById(R.id.btnNewMessage);
		
		try {
			smsManager = new SmsMessageManager(context);

			// Set list adapter
			// If show all is enable, show thread conversations
			if (showAll) {
				adapter = new CustomConversationListAdapter(context);
			} else {
				adapter = new CustomListAdapter(context, showAll);
			}

			// Set listview adapter
			listView.setAdapter(adapter);

			// Set listener
			btnNewMessage.setOnClickListener(this);

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// Set current index
					index = position;

					// If conversation
					Class<?> _class;
					if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
						_class = ComposeSmsActivity.class;
					} else {
						_class = ComposeSmsKitkatActivity.class;
					}
					if (showAll) {
						ConversationModel item = (ConversationModel) listView
								.getItemAtPosition(position);
						Intent intent = new Intent(context, _class);
						intent.putExtra(ComposeSmsActivity.THREAD_ID,
								item.getId());
						intent.putExtra(ComposeSmsActivity.THREAD_UNREAD_COUNT,
								item.getUnreadCount());
						intent.putExtra(ComposeSmsActivity.SENDER_NAME,
								item.getSender());
						intent.putExtra(ConstantUtil.ACTIVITY_ACTION_TYPE,
								ConstantUtil.THREAD_DETAIL_MESSAGE_ACTION);
						// Set conversation to read
						item.setUnreadCount(0);
						startActivityForResult(intent,
								ACTIVITY_COMPOSE_SMS_RESULT);
					} else {
						// Display single message
						SmsModel item = (SmsModel) listView
								.getItemAtPosition(position);
						Intent intent = new Intent(context, _class);
						intent.putExtra(ComposeSmsActivity.THREAD_ID,
								item.getThreadId());
						intent.putExtra(ComposeSmsActivity.MESSAGE_ID,
								item.getId());
						intent.putExtra(ComposeSmsActivity.SENDER_NAME,
								item.getPhone());
						intent.putExtra(ConstantUtil.ACTIVITY_ACTION_TYPE,
								ConstantUtil.MESSAGE_DETAIL_ACTION);
						startActivity(intent);
					}
				}
			});

			// Register context menu
			this.registerForContextMenu(listView);
		} catch (Exception ex) {
			Log.e(TAG, "error", ex);
		}

		return v;
	}

	public void applySearch(String searchString) {
		if (showAll) {
			CustomConversationListAdapter conversationAdapter = ((CustomConversationListAdapter) adapter);
			conversationAdapter.search(searchString);
			conversationAdapter.notifyDataSetChanged();
		} else {
			CustomListAdapter messageAdapter = ((CustomListAdapter) adapter);
			messageAdapter.search(searchString);
			messageAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
		if (v.getId() == R.id.lvMessages) {
			menu.add(Menu.NONE, CONTEXT_ITEM_DELETE, 0,
					this.getString(R.string.context_item_delete));

			menu.add(Menu.NONE, CONTEXT_ITEM_BLOCK, 1,
					this.getString(R.string.context_item_block));
			if (!showAll) {
				menu.add(Menu.NONE, CONTEXT_ITEM_UNSPAM, 2,
						this.getString(R.string.context_item_unspam));
			}
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
				if (showAll) {
					// Process for conversation items
					ConversationModel conv = (ConversationModel) adapter
							.getItem(menuInfo.position);
					smsManager.deleteMessageByConversation(String.valueOf(conv
							.getId()));
					((CustomConversationListAdapter) adapter)
							.removeItem(menuInfo.position);
				} else {
					// Process for message items
					SmsModel sms = (SmsModel) adapter
							.getItem(menuInfo.position);
					//smsManager.deleteMessage(sms.getId());
					//Delete spam message
					DatabaseManagerUtil.getInstance(this.context)
					.deleteSpamMessage(sms.getId());
					((CustomListAdapter) adapter).removeItem(menuInfo.position);
				}

				adapter.notifyDataSetChanged();
			} catch (Exception ex) {
			}
			break;
		case CONTEXT_ITEM_BLOCK:
			try {
				// Load phone filter settings
				loadSettings();

				if (showAll) {
					ConversationModel conv = (ConversationModel) adapter
							.getItem(menuInfo.position);
					blockPerson(conv.getSender());
				} else {
					// Get sms
					SmsModel sms = (SmsModel) adapter
							.getItem(menuInfo.position);
					blockPerson(sms.getPhone());
				}
			} catch (Exception ex) {
			}
			break;
		case CONTEXT_ITEM_UNSPAM:
			try {
				// Get sms
				SmsModel sms = (SmsModel) adapter.getItem(menuInfo.position);
				sms.setMessage(sms.getMessage().replace(
						SmsMessageManager.MESSAGE_SPAM_PREFIX, ""));
				sms.setType(SmsMessageManager.MESSAGE_TYPE_INBOX);
				smsManager.insertSms(sms);
				DatabaseManagerUtil.getInstance(this.context)
						.deleteSpamMessage(sms.getId());
				((CustomListAdapter) adapter).removeItem(menuInfo.position);
				adapter.notifyDataSetChanged();
				deletePhoneSetting(sms.getPhone());
			} catch (Exception ex) {
			}
			break;
		}
		return true;
	}

	private void deletePhoneSetting(String value) {
		String original = SmsMessageFilter.getIntance(this.context)
				.FormatPhoneNumber(value);
		DatabaseManagerUtil.getInstance(this.context).deleteSettingByValue(
				value);
		DatabaseManagerUtil.getInstance(this.context).deleteSettingByValue(
				original);
	}

	private void blockPerson(String sender) {
		Toast toast = null;
		if (phoneFilters.contains(sender)) {
			toast = Toast.makeText(context,
					this.getString(R.string.message_toast_phone_existed),
					Toast.LENGTH_LONG);
			toast.show();
		} else {
			this.phoneFilters.add(sender);
			FilterSettingModel setting = new FilterSettingModel();
			setting.setType(MessageFilterContent.SETTING_TYPE_PHONE);
			setting.setValue(sender);

			// Insert
			DatabaseManagerUtil.getInstance(this.context)
					.insertSetting(setting);

			toast = Toast.makeText(context,
					this.getString(R.string.message_toast_filter_blocked),
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnNewMessage:
			Class<?> _class;
			if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				_class = ComposeNewSmsActivity.class;
			} else {
				_class = ComposeNewSmsKitkatActivity.class;
			}
			Intent i = new Intent(this.context, _class);
			startActivityForResult(i, ACTIVITY_COMPOSE_SMS_RESULT);
			break;
		default:
			break;
		}
	}

	private void loadSettings() {
		// Load settings
		List<FilterSettingModel> settings = DatabaseManagerUtil.getInstance(
				this.context).getSettingsByType(
				MessageFilterContent.SETTING_TYPE_PHONE);

		phoneFilters = new HashSet<String>();

		for (FilterSettingModel setting : settings) {
			phoneFilters.add(setting.getValue());
		}
	}
}