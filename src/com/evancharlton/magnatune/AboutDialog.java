package com.evancharlton.magnatune;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

public class AboutDialog extends Dialog {
	public static final int DIALOG_ID = 0xDEADBE47;

	public AboutDialog(Context context) {
		super(context);
		setContentView(R.layout.about);

		Button close = (Button) findViewById(R.id.close_about_btn);
		close.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});

		((Button) findViewById(R.id.magnatune_about_btn)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getContext().startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://magnatune.com/")));
			}
		});

		String title = getContext().getString(R.string.app_name);
		try {
			title += " version " + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_ACTIVITIES).versionName;
		} catch (NameNotFoundException e) {
			title += " Unknown version";
		}
		setTitle(title);
	}

	public static AboutDialog create(Context context) {
		return new AboutDialog(context);
	}
}