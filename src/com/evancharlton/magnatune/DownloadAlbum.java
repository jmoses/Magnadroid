package com.evancharlton.magnatune;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.evancharlton.magnatune.objects.Download;
import com.evancharlton.magnatune.objects.Song;

public class DownloadAlbum extends AlbumBrowser implements MediaScannerConnectionClient {
	private static final String TAG = "Magnatune_DownloadAlbum";

	private static final int DIALOG_FORMAT = 1;
	private static final int DIALOG_NETWORK = 2;
	private static final int DIALOG_SD_CARD = 3;

	private String mUsername;
	private String mPassword;

	private ProgressBar mProgressBar;
	private TextView mStatusText;

	private SimpleAdapter mFormatAdapter;
	private ArrayList<HashMap<String, String>> mFormatData = new ArrayList<HashMap<String, String>>();

	private DownloadTask mDownloadTask;

	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
	private MediaScannerConnection mScanner;

	public void onCreate(Bundle savedInstanceState) {
		mFormatAdapter = new SimpleAdapter(this, mFormatData, android.R.layout.select_dialog_item, new String[] {
			Download.Format.TEXT
		}, new int[] {
			android.R.id.text1
		});

		Intent intent = getIntent();
		mArtist = intent.getStringExtra(Download.ARTIST);
		mAlbum = intent.getStringExtra(Download.ALBUM);

		// TODO: Make these share a layout?
		super.onCreate(savedInstanceState, R.layout.download_album);

		mStatusText = (TextView) findViewById(R.id.status);
		mProgressBar = (ProgressBar) findViewById(R.id.progress);

		mStatusText.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);

		mPurchaseButton.setText(getString(R.string.download));
		mPurchaseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mPurchaseButton.setEnabled(false);
				showDialog(DIALOG_FORMAT);
			}
		});

		Object[] saved = (Object[]) getLastNonConfigurationInstance();
		if (saved != null) {
			mDownloadTask = (DownloadTask) saved[saved.length - 1];
			if (mDownloadTask != null) {
				mDownloadTask.activity = this;
			}
		}

		mUsername = intent.getStringExtra(Download.USERNAME);
		mPassword = intent.getStringExtra(Download.PASSWORD);

		String fmt;
		for (HashMap<String, String> format : Download.FORMATS) {
			fmt = format.get(Download.Format.TYPE);
			format.put(Download.Format.URL, intent.getStringExtra(fmt));
			mFormatData.add(format);
			mFormatAdapter.notifyDataSetChanged();
		}

		// see if they're on edge or gprs
		ConnectivityManager mgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = mgr.getActiveNetworkInfo();
		if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			showDialog(DIALOG_NETWORK);
		}

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			showDialog(DIALOG_SD_CARD);
			mPurchaseButton.setEnabled(false);
		}

		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Object[] objects = (Object[]) super.onRetainNonConfigurationInstance();
		Object[] saved = new Object[objects.length + 1];
		int i;
		for (i = 0; i < objects.length; i++) {
			saved[i] = objects[i];
		}
		saved[i] = mDownloadTask;
		return saved;
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseWakeLock();
		if (mScanner.isConnected()) {
			mScanner.disconnect();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mScanner = new MediaScannerConnection(this, this);
		mScanner.connect();
	}

	public void scanFile(String path, String mimeType) {
		mScanner.scanFile(path, mimeType);
	}

	protected void done() {
		mScanner.disconnect();
		mStatusText.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);
	}

	protected void startProgress() {
		mStatusText.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	protected void update(String status, int pos, int max) {
		startProgress();
		mStatusText.setText(status);
		mProgressBar.setIndeterminate(pos == -1 || max == -1);
		if (!mProgressBar.isIndeterminate()) {
			mProgressBar.setMax(max);
			mProgressBar.setProgress(pos);
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onMediaScannerConnected() {
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		if (uri != null) {
			try {
				Cursor c = managedQuery(uri, new String[] {
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION
				}, null, null, null);
				if (c.getCount() == 1) {
					c.moveToFirst();
					HashMap<String, String> info = new HashMap<String, String>();
					info.put(Song.TITLE, c.getString(0));
					info.put(Song.MP3, path);
					info.put(Song.DURATION_TEXT, getDuration(c.getLong(1) / 1000));

					mAdapterData.add(info);
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cancel();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void cancel() {
		if (mDownloadTask != null && mDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
			mDownloadTask.cancel(true);
		}
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_FORMAT:
				return new AlertDialog.Builder(this).setAdapter(mFormatAdapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mDownloadTask = new DownloadTask(mUsername, mPassword, mAlbum, mArtist);
						mDownloadTask.activity = DownloadAlbum.this;
						HashMap<String, String> info = mFormatData.get(which);
						mDownloadTask.execute(info.get(Download.Format.URL));
						removeDialog(DIALOG_FORMAT);
					}
				}).setTitle("Format").create();
			case DIALOG_NETWORK:
				return new AlertDialog.Builder(this).setNegativeButton(R.string.wifi_settings, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_NETWORK);
						Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
						startActivity(intent);
						finish();
					}
				}).setTitle(R.string.warning_mobile_network_title).setMessage(R.string.warning_mobile_network_message).setPositiveButton(R.string.continue_msg, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_NETWORK);
					}
				}).create();
			case DIALOG_SD_CARD:
				return new AlertDialog.Builder(this).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_SD_CARD);
					}
				}).setTitle(R.string.error_missing_sd_card_title).setMessage(R.string.error_missing_sd_card_message).create();
		}
		return super.onCreateDialog(which);
	}

	protected void acquireWakeLock() {
		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}
	}

	protected void releaseWakeLock() {
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		HashMap<String, String> info = mAdapterData.get(position);
		String url = info.get(Song.MP3);
		try {
			mController.autoPlay(url);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
