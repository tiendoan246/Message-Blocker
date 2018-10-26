package com.tsoft.messenger;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

	private Button btnRating;
	private TextView tvVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		btnRating = (Button) findViewById(R.id.btnRating);
		tvVersion = (TextView) findViewById(R.id.tvVersion);

		btnRating.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("market://details?id=" + getPackageName());
				Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
				try {
					startActivity(myAppLinkToMarket);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							AboutActivity.this,
							AboutActivity.this
									.getString(R.string.about_rate_unable_start),
							Toast.LENGTH_LONG).show();
				}

			}
		});

		// Set version info
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			tvVersion.setText(getString(R.string.version) + " "
					+ pInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
