package com.tsoft.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsoft.callback.CustomDialogListener;
import com.tsoft.control.FloatingActionButton;
import com.tsoft.control.FloatingActionMenu;
import com.tsoft.control.SlideInAnimationHandler;
import com.tsoft.control.SubActionButton;
import com.tsoft.datamodel.FilterSettingModel;
import com.tsoft.dialog.ContentPromptDialog;
import com.tsoft.dialog.PromptDialog;
import com.tsoft.messenger.R;
import com.tsoft.util.DatabaseManagerUtil;

public class MessageFilterContent extends Fragment implements OnClickListener {

	private Context _context;
	private FloatingActionMenu centerBottomMenu;
	private FloatingActionButton darkButton;

	private ListView listView;

	private Set<String> contentFilters;
	private ArrayAdapter<String> adapter;
	private List<String> filter;

	// Context menu
	public static final int CONTEXT_ITEM_DELETE = 1;

	private static final int BUTTON_PHONE_ID = 11;
	private static final int BUTTON_CONTENT_ID = 22;
	//private static final int BUTTON_SPAM_ID = 33;

	public static final int SETTING_TYPE_PHONE = 1;
	public static final int SETTING_TYPE_CONTENT = 2;
	
	public MessageFilterContent(Context context) {
		this._context = context;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_menu_with_custom_animation, container, false);

		try {
			listView = (ListView) rootView.findViewById(R.id.lvMessageFilters);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

			ImageView fabContent = new ImageView(getActivity());
			fabContent.setImageDrawable(ContextCompat.getDrawable(
					this._context, R.drawable.ic_spam));

			darkButton = new FloatingActionButton.Builder(getActivity())
					.setTheme(FloatingActionButton.THEME_DARK)
					.setContentView(fabContent)
					.setPosition(FloatingActionButton.POSITION_BOTTOM_RIGHT)
					.build();

			SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(
					getActivity()).setTheme(SubActionButton.THEME_DARK);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(120,
					120);
			rLSubBuilder.setLayoutParams(params);

			ImageView rlIcon1 = new ImageView(getActivity());
			ImageView rlIcon2 = new ImageView(getActivity());
			//ImageView rlIcon3 = new ImageView(getActivity());

			rlIcon1.setImageDrawable(ContextCompat.getDrawable(this._context,
					R.drawable.ic_phone_24));
			rlIcon2.setImageDrawable(ContextCompat.getDrawable(this._context,
					R.drawable.ic_content_24));
			//rlIcon3.setImageDrawable(ContextCompat.getDrawable(this._context,
			//		R.drawable.ic_spam_24));

			SubActionButton btnPhone = rLSubBuilder.setContentView(rlIcon1)
					.build();
			btnPhone.setId(BUTTON_PHONE_ID);
			SubActionButton btnContent = rLSubBuilder.setContentView(rlIcon2)
					.build();
			btnContent.setId(BUTTON_CONTENT_ID);
			//SubActionButton btnSpam = rLSubBuilder.setContentView(rlIcon3)
			//		.build();
			//btnSpam.setId(BUTTON_SPAM_ID);

			btnPhone.setOnClickListener(this);
			btnContent.setOnClickListener(this);
			//btnSpam.setOnClickListener(this);

			centerBottomMenu = new FloatingActionMenu.Builder(getActivity())
					.setStartAngle(180).setEndAngle(270)
					.setAnimationHandler(new SlideInAnimationHandler())
					.addSubActionView(btnPhone).addSubActionView(btnContent)
					//.addSubActionView(btnSpam)
					.attachTo(darkButton).build();

			// Load settings
			loadSettings();

			// Set list adapter
			adapter = new ArrayAdapter<String>(this._context,
					android.R.layout.simple_list_item_checked, filter) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View view = super.getView(position, convertView, parent);
					TextView text = (TextView) view
							.findViewById(android.R.id.text1);
					text.setTextColor(Color.GRAY);
					return view;
				}
			};

			// Register context menu
			this.registerForContextMenu(listView);
			listView.setAdapter(adapter);
		} catch (Exception ex) {
		}
		return rootView;
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
					new Handler().post(new Runnable() {
				        public void run() {
				        	if (centerBottomMenu != null) {
				    			centerBottomMenu.close(true);
				    		}
				    		if (darkButton != null) {
				    			darkButton.detach();
				    		}
				        }
				    });
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
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
		if (v.getId() == R.id.lvMessageFilters) {
			menu.add(Menu.NONE, CONTEXT_ITEM_DELETE, 0,
					this.getString(R.string.context_item_delete));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Get extra info about list item that was long-pressed
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Toast toast = null;

		// Perform action according to selected item from context menu
		switch (item.getItemId()) {
		case CONTEXT_ITEM_DELETE:
			try {
				String selectedvalue = adapter.getItem(menuInfo.position);
				if (contentFilters.contains(selectedvalue)) {
					contentFilters.remove(selectedvalue);
					adapter.remove(selectedvalue);

					//Delete setting
					DatabaseManagerUtil.getInstance(this._context).deleteSettingByValue(selectedvalue);

					toast = Toast.makeText(this._context, this
							.getString(R.string.message_toast_filter_deleted),
							Toast.LENGTH_SHORT);
					toast.show();
					loadSettings();
					adapter.notifyDataSetChanged();
				}
			} catch (Exception ex) {
			}
			break;
		}
		return true;
	}

	private void loadSettings() {
		filter = new ArrayList<String>();
		List<FilterSettingModel> settings = DatabaseManagerUtil.getInstance(this._context).getSettings();
		for (FilterSettingModel setting : settings) {
			filter.add(setting.getValue());
		}
		contentFilters = new HashSet<String>(filter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case BUTTON_PHONE_ID:
			final PromptDialog phoneInputDialog = new PromptDialog(this._context,
					android.R.style.Theme_Holo_Light_Dialog, contentFilters);
			phoneInputDialog.setDialogListener(new CustomDialogListener() {

				@Override
				public void userSelectedValue(String value) {
					adapter.add(value);
					adapter.notifyDataSetChanged();
				}

				@Override
				public void userCanceled() {
					// TODO Auto-generated method stub
				}
			});
			phoneInputDialog.show();
			break;
		case BUTTON_CONTENT_ID:
			final ContentPromptDialog contentInputDialog = new ContentPromptDialog(
					this._context, android.R.style.Theme_Holo_Light_Dialog,
					contentFilters);
			contentInputDialog.setDialogListener(new CustomDialogListener() {

				@Override
				public void userSelectedValue(String value) {
					adapter.add(value);
					adapter.notifyDataSetChanged();
				}

				@Override
				public void userCanceled() {
					// TODO Auto-generated method stub
				}
			});
			contentInputDialog.show();
			break;
		//case BUTTON_SPAM_ID:
		//	Intent iSpam = new Intent(this._context, SpamMessageActivity.class);
		//	startActivity(iSpam);
		//	break;
		}
	}

}
