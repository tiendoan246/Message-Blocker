package com.tsoft.adapter;

import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tsoft.datamodel.ConversationModel;
import com.tsoft.datamodel.PhoneModel;
import com.tsoft.messenger.R;
import com.tsoft.util.ColorGenerator;
import com.tsoft.util.ConversationUtil;
import com.tsoft.util.PhoneManagerUtil;
import com.tsoft.util.SmsMessageManager;
import com.tsoft.util.TextDrawable;

public class CustomConversationListAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<ConversationModel> conversations;
	private ConversationUtil conversationUtil;
	private SmsMessageManager messageManager;
	private String _search = "";

	// declare the color generator and drawable builder
	private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;

	public CustomConversationListAdapter(Context context) {
		// Create message conversation util
		conversationUtil = ConversationUtil.getInstance(context);
		this.messageManager = new SmsMessageManager(context);
		this.context = context;

		// Load conversation
		this.conversations = conversationUtil.getAllThreads();
	}

	public CustomConversationListAdapter(Context context,
			ArrayList<ConversationModel> conversations) {
		this.context = context;
		this.conversations = conversations;
	}

	public void refresh() {
		// Load conversation
		this.conversations = conversationUtil.getAllThreads();
	}

	public void search(String search) {
		this._search = search;
		this.conversations = conversationUtil.getAllThreads(search);
	}

	@Override
	public int getCount() {
		return this.conversations.size();
	}

	@Override
	public Object getItem(int position) {
		return this.conversations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return ((ConversationModel) getItem(position)).getId();
	}

	public void removeItem(int location) {
		this.conversations.remove(getItem(location));
	}

	public void insert(ConversationModel item, int position) {
		this.conversations.set(position, item);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// getting movie data for the row
		ConversationModel conv = conversations.get(position);

		// The child views in each row.
		ImageView thumbNail;
		TextView tvphone, tvbody, tvdate;

		if (inflater == null)
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_row, null);

			// Find the child views.
			thumbNail = (ImageView) convertView.findViewById(R.id.ivThumbnail);
			tvphone = (TextView) convertView.findViewById(R.id.tvPhone);
			tvbody = (TextView) convertView.findViewById(R.id.tvBody);
			tvdate = (TextView) convertView.findViewById(R.id.tvDate);

			// Optimization: Tag the row with it's child views, so we don't have
			// to
			// call findViewById() later when we reuse the row.
			convertView.setTag(new ConversationViewHolder(thumbNail, tvphone,
					tvbody, tvdate));
		} else {
			// Because we use a ViewHolder, we avoid having to call
			// findViewById().
			ConversationViewHolder viewHolder = (ConversationViewHolder) convertView
					.getTag();
			thumbNail = viewHolder.getThumbNail();
			tvphone = viewHolder.getTvphone();
			tvbody = viewHolder.getTvbody();
			tvdate = viewHolder.getTvdate();
		}

		TextDrawable drawable = null;

		// phone
		// Replace VN region
		if (conv.getSender() != null) {
			String phone = conv.getSender();
			PhoneModel phoneModel = PhoneManagerUtil.getIntance(this.context)
					.getPhoneByNumber(phone);
			tvphone.setText(phoneModel != null ? phoneModel.getDisplayName()
					: conv.getSender());
			// The contact is exist
			if (phoneModel != null) {
				drawable = TextDrawable.builder().buildRound(
						String.valueOf(phoneModel.getDisplayName().charAt(0)),
						mColorGenerator.getColor(phoneModel.getDisplayName()));
			} else {
				drawable = TextDrawable.builder().buildRound(
						ContextCompat.getDrawable(context,
								R.drawable.contact_center),
						mColorGenerator.getColor(conv.getSender()));
			}
		}

		// thumbnail image
		thumbNail.setImageDrawable(drawable);

		// body
		tvbody.setText(conv.getSnippet());

		// date
		// Date dateValue = new Date(conv.getDate());
		// SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
		tvdate.setTextColor(Color.GRAY);
		tvdate.setText(MessageFormat.format("({0})", conv.getMessageCount()));

		// Set unread count
		int count = messageManager.getUnreadMessagesCount(String.valueOf(conv
				.getId()));
		conv.setUnreadCount(count);
		if (count > 0) {
			tvphone.setTextColor(Color.BLACK);
			tvbody.setTextColor(Color.BLACK);
			tvbody.setTypeface(null, Typeface.BOLD);
		} else {
			tvphone.setTextColor(Color.GRAY);
			tvbody.setTextColor(Color.GRAY);
			tvbody.setTypeface(null, Typeface.NORMAL);
		}

		return convertView;
	}

	public static CharSequence highlight(String search, String originalText) {
		// ignore case and accents
		// the same thing should have been done for the search text
		String normalizedText = Normalizer
				.normalize(originalText, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.toLowerCase();

		int start = normalizedText.indexOf(search);
		if (start < 0) {
			// not found, nothing to to
			return originalText;
		} else {
			// highlight each appearance in the original text
			// while searching in normalized text
			Spannable highlighted = new SpannableString(originalText);
			while (start >= 0) {
				int spanStart = Math.min(start, originalText.length());
				int spanEnd = Math.min(start + search.length(),
						originalText.length());

				highlighted.setSpan(new BackgroundColorSpan(Color.WHITE),
						spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				start = normalizedText.indexOf(search, spanEnd);
			}

			return highlighted;
		}
	}

	/** Holds child views for one row. */
	public static class ConversationViewHolder {
		private ImageView thumbNail;
		private TextView tvphone, tvbody, tvdate;

		public ConversationViewHolder(ImageView thumbNail, TextView tvphone,
				TextView tvbody, TextView tvdate) {
			this.thumbNail = thumbNail;
			this.tvphone = tvphone;
			this.tvbody = tvbody;
			this.tvdate = tvdate;
		}

		public ImageView getThumbNail() {
			return thumbNail;
		}

		public void setThumbNail(ImageView thumbNail) {
			this.thumbNail = thumbNail;
		}

		public TextView getTvphone() {
			return tvphone;
		}

		public void setTvphone(TextView tvphone) {
			this.tvphone = tvphone;
		}

		public TextView getTvbody() {
			return tvbody;
		}

		public void setTvbody(TextView tvbody) {
			this.tvbody = tvbody;
		}

		public TextView getTvdate() {
			return tvdate;
		}

		public void setTvdate(TextView tvdate) {
			this.tvdate = tvdate;
		}

		public ConversationViewHolder() {
		}
	}
}
