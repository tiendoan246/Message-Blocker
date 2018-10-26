package com.tsoft.control;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;

public class DelayAutoCompleteTextView extends MultiAutoCompleteTextView {
	private static final int MESSAGE_TEXT_CHANGED = 100;
	private static final int DEFAULT_AUTOCOMPLETE_DELAY = 750;

	private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
	private ProgressBar mLoadingIndicator;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			DelayAutoCompleteTextView.super.performFiltering(
					(CharSequence) msg.obj, msg.arg1);
		}
	};

	public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setLoadingIndicator(ProgressBar progressBar) {
		mLoadingIndicator = progressBar;
	}

	public void setAutoCompleteDelay(int autoCompleteDelay) {
		mAutoCompleteDelay = autoCompleteDelay;
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		if (mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.VISIBLE);
		}
		// The n items
		//String str = text.toString();
		//if (str.contains(";") && !str.endsWith(";")) {
		//	text = text.subSequence(str.lastIndexOf(";") + 1, str.length());
		//}
		mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
		mHandler.sendMessageDelayed(
				mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text),
				mAutoCompleteDelay);
	}

	@Override
	public void onFilterComplete(int count) {
		if (mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.GONE);
		}
		super.onFilterComplete(count);
	}

}
