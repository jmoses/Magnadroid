package com.evancharlton.magnatune;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class Settings extends PreferenceActivity {
	private static final int MENU_CACHE = Menu.FIRST;

	private static final int DIALOG_CACHE_DELETED = 10;
	private static final int DIALOG_CACHE_DELETING = 11;

	private ProgressDialog mDeleteDialog;
	private DeleteCacheTask mDeleteTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_CACHE, Menu.NONE, R.string.menu_clear_cache).setIcon(R.drawable.ic_menu_delete);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_CACHE:
				mDeleteTask = new DeleteCacheTask();
				mDeleteTask.activity = this;
				mDeleteTask.execute();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CACHE_DELETED:
				return new AlertDialog.Builder(this).setTitle(R.string.cache_cleared).setPositiveButton(android.R.string.ok, null).create();
			case DIALOG_CACHE_DELETING:
				if (mDeleteDialog == null) {
					mDeleteDialog = new ProgressDialog(this);
					mDeleteDialog.setTitle(R.string.cache_clearing);
					mDeleteDialog.setMessage(getString(R.string.cache_clearing_message));
					mDeleteDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					mDeleteDialog.setIndeterminate(true);
					mDeleteDialog.setCancelable(false);
				}
				return mDeleteDialog;
		}
		return super.onCreateDialog(id);
	}

	private static class DeleteCacheTask extends AsyncTask<String, Integer, Boolean> {
		public Settings activity;

		@Override
		protected void onPreExecute() {
			activity.showDialog(DIALOG_CACHE_DELETING);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			File folder = new File(MagnatuneAPI.CACHE_DIRECTORY);
			File[] files = folder.listFiles();
			File file;
			int length = files.length;
			for (int i = 0; i < length; i++) {
				file = files[i];
				if (file.isFile()) {
					file.delete();
					publishProgress(i, length);
				}
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			activity.mDeleteDialog.setIndeterminate(false);
			activity.mDeleteDialog.setMax(progress[1]);
			activity.mDeleteDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			activity.removeDialog(DIALOG_CACHE_DELETING);
			activity.showDialog(DIALOG_CACHE_DELETED);
		}
	}
}
