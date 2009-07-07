package com.evancharlton.magnatune;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class LazyActivity extends Activity implements OnItemClickListener {
	protected static final int DIALOG_ERROR_LOADING = 10;

	protected ListView mList;
	protected LazyAdapter mAdapter;
	protected List<HashMap<String, String>> mAdapterData = new ArrayList<HashMap<String, String>>();
	protected String[] mFrom;
	protected int[] mTo;

	protected LoadTask mLoadTask;

	protected static int mPage = 1;

	protected void onCreate(Bundle savedInstanceState, int layoutResId) {
		onCreate(savedInstanceState, layoutResId, android.R.layout.simple_list_item_1);
	}

	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState, int layoutResId, int rowLayoutResId) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(layoutResId);

		mList = (ListView) findViewById(android.R.id.list);
		mAdapter = new LazyAdapter(this, mAdapterData, rowLayoutResId, mFrom, mTo);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);

		// restore state
		Object[] saved = (Object[]) getLastNonConfigurationInstance();
		if (saved != null) {
			mAdapterData.addAll((Collection<? extends HashMap<String, String>>) saved[0]);
			mAdapter.notifyDataSetChanged();
			mLoadTask = (LoadTask) saved[1];
		}

		if (mLoadTask == null) {
			mLoadTask = newLoadTask();
		}
		setTaskActivity();

		if (mLoadTask.getStatus() == AsyncTask.Status.PENDING) {
			mLoadTask.execute();
		}
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_ERROR_LOADING:
				return new AlertDialog.Builder(this).setTitle(R.string.error_loading_title).setMessage(R.string.error_loading_message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_ERROR_LOADING);
					}
				}).create();
		}
		return super.onCreateDialog(which);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return new Object[] {
				mAdapterData,
				mLoadTask
		};
	}

	final public void loadNextPage() {
		mPage++;
		mLoadTask = newLoadTask();
		setTaskActivity();
		mLoadTask.execute();
	}

	final public boolean isTaskFinished() {
		return mLoadTask.getStatus() == AsyncTask.Status.FINISHED;
	}

	abstract protected LoadTask newLoadTask();

	protected void setTaskActivity() {
		mLoadTask.activity = this;
	}

	protected void startActivityForPosition(Class<?> targetCls, int position) {
		HashMap<String, String> info = mAdapterData.get(position);
		Intent i = new Intent(this, targetCls);
		for (String key : info.keySet()) {
			i.putExtra(key, info.get(key));
		}
		startActivity(i);
	}

	protected abstract static class LoadTask extends AsyncTask<String, HashMap<String, String>, Boolean> {
		public LazyActivity activity;
		protected boolean mCancelled;

		@Override
		protected void onPreExecute() {
			mCancelled = false;
			activity.setProgressBarIndeterminateVisibility(true);
			activity.mAdapter.setStopLoading(true);
		}

		@Override
		protected void onProgressUpdate(HashMap<String, String>... updates) {
			activity.mAdapterData.add(updates[0]);
			activity.mAdapter.notifyDataSetChanged();
			activity.mAdapter.setStopLoading(false);
		}

		@Override
		protected void onCancelled() {
			mCancelled = true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			activity.setProgressBarIndeterminateVisibility(false);
		}
	}
}
