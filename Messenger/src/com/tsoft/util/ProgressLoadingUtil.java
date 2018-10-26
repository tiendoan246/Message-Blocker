package com.tsoft.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

public class ProgressLoadingUtil {

	private Context _context;
	private static ProgressLoadingUtil _instance;

	private ProgressDialog progressLoading;

	private ProgressLoadingUtil(Context context) {
		this._context = context;
		this.progressLoading = new ProgressDialog(_context,
				android.R.style.Theme_Holo_Light_Dialog);
		this.progressLoading.getWindow().setBackgroundDrawable(
				new ColorDrawable(0));
		this.progressLoading.setCanceledOnTouchOutside(false);
		this.progressLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.progressLoading.setCancelable(false);
	}

	public static ProgressLoadingUtil getInstance(Context context) {
		if (_instance == null) {
			_instance = new ProgressLoadingUtil(context);
		}
		return _instance;
	}

	public void showLoadingSpinner() {
		this.progressLoading.show();
	}

	public void hideLoadingSpinner() {
		this.progressLoading.dismiss();
	}
}
