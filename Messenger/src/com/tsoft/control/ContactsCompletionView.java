package com.tsoft.control;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tsoft.datamodel.PhoneModel;
import com.tsoft.messenger.R;

public class ContactsCompletionView extends TokenCompleteTextView<PhoneModel> {

	private ProgressBar mLoadingIndicator;

	public ContactsCompletionView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setLoadingIndicator(ProgressBar progressBar) {
		mLoadingIndicator = progressBar;
	}

	@Override
	protected View getViewForObject(PhoneModel phone) {

		LayoutInflater l = (LayoutInflater) getContext().getSystemService(
				Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) l.inflate(R.layout.contact_token,
				(ViewGroup) ContactsCompletionView.this.getParent(), false);
		((TextView) view.findViewById(R.id.tvTockenName)).setText(phone
				.getDisplayName());

		return view;
	}

	@Override
	protected PhoneModel defaultObject(String completionText) {
		PhoneModel phone = new PhoneModel();
		phone.setDisplayName(completionText);
		phone.setNumber(completionText);
		return phone;
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		if (mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.VISIBLE);
		}
		super.performFiltering(text, keyCode);
	}

	@Override
	public void onFilterComplete(int count) {
		if (mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.GONE);
		}
		super.onFilterComplete(count);
	}
}
